package com.boeckerman.jake.protobuf;

import com.boeckerman.jake.protobuf.filecoordinates.GeneratedResponseFileCoordinates;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Label;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.File;

import java.util.stream.Stream;

public class ListFields implements FieldHandler {
    private final Context.FieldContext fieldContext;
    private final Extensions.JavaFieldExtension.ListOptions listOptions;
    private final DescriptorProtos.FieldDescriptorProto fieldDescriptorProto;
    private final NameVariants names;
    private final TypeUtils.TypeNames typeNames;

    public ListFields(Context.FieldContext fieldContext) {
        this.fieldContext = fieldContext;
        this.listOptions = fieldContext.fieldExtension().getList();
        this.fieldDescriptorProto = fieldContext.fieldDescriptorProto();

        this.names = new NameVariants(fieldContext);
        this.typeNames = fieldContext.executionContext().typeNames().apply(fieldDescriptorProto);
    }

    @Override
    public Stream<File> get() {
        if (fieldDescriptorProto.getLabel() != Label.LABEL_REPEATED) {
            return Stream.empty();
        }
        Stream.Builder<File> builder = Stream.builder();

        if (listOptions.getAddAllAcceptsStream()) {
            builder.add(addAllAcceptsStream());
        }
//        if (listOptions.getFriendlyGetter()) {
//            builder.add(friendlyGetter());
//        }
//        if (listOptions.getStreamGetter()) {
//            builder.add(streamGetter());
//        }
        return builder.build();
    }


    private File addAllAcceptsStream() {
        return builderContext("""
                void %s (Stream<%s> value) // Stream-friendly addAll
                {
                    %s
                }
                """.formatted("addAll" + names.name(),
                typeNames.boxed(),
                methodInvoke("addAll", names.name(), "value.collect(java.util.stream.Collectors.toList())")));
    }

    private File friendlyGetter() {
        return null;
    }

    private File streamGetter() {
        return null;
    }

    @Override
    public GeneratedResponseFileCoordinates context() {
        return fieldContext;
    }
}
