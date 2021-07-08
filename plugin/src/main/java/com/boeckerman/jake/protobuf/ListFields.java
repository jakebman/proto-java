package com.boeckerman.jake.protobuf;

import com.boeckerman.jake.protobuf.filecoordinates.GeneratedResponseFileCoordinates;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.File;

import java.util.stream.Stream;

import static com.boeckerman.jake.protobuf.CodeGeneratorUtils.CamelCase;

public class ListFields implements FieldHandler {
    private final Context.FieldContext fieldContext;
    private final Extensions.JavaFieldExtension.ListOptions listOptions;
    private final DescriptorProtos.FieldDescriptorProto fieldDescriptorProto;
    private final String protoName;

    public ListFields(Context.FieldContext fieldContext) {
        this.fieldContext = fieldContext;
        this.listOptions = fieldContext.fieldExtension().getList();
        this.fieldDescriptorProto = fieldContext.fieldDescriptorProto();

        this.protoName = CamelCase(fieldDescriptorProto.getName());
    }

    @Override
    public Stream<File> get() {
        // stub
        return Stream.empty();
//        if (fieldDescriptorProto.getLabel() != LABEL_REPEATED) {
//            return Stream.empty();
//        }
//        Stream.Builder<File> builder = Stream.builder();
//
//        if (listOptions.getAddAllAcceptsStream()) {
//            builder.add(addAllAcceptsStream());
//        }
//        if (listOptions.getFriendlyGetter()) {
//            builder.add(friendlyGetter());
//        }
//        if (listOptions.getStreamGetter()) {
//            builder.add(streamGetter());
//        }
//        return builder.build();
    }


    private File addAllAcceptsStream() {
        return builderContext("""
                void %s (Stream<%s> value) // Stream-friendly addAll
                {
                    if(value == null) %s;
                    else %s;
                }
                """.formatted(
                methodDeclarationHeader("void", "addAll", protoName, "BROKEN!!!" + " value"),
                methodInvoke("clear", protoName),
                methodInvoke("set", protoName, "value")));
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
