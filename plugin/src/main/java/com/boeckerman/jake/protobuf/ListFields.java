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
        TypeUtils.TypeReference typeReference = fieldContext.executionContext().typeNames();
        this.typeNames = typeReference.apply(fieldDescriptorProto);
        if(this.typeNames == null) {
            throw new Error(typeReference + " does not have " + fieldDescriptorProto);
        }
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
        if (listOptions.getFriendlyGetter()) {
            builder.add(friendlyGetter());
        }
        if (listOptions.getStreamGetter()) {
            builder.add(streamGetter());
        }
        return builder.build();
    }


    private File addAllAcceptsStream() {
        // nb: `%1$s` references the first argument to formatted
        return builderContext("""
                void addAll%1$s(Stream<%2$s> value) // Stream-friendly addAll
                {
                    addAll%1$s(value.collect(java.util.stream.Collectors.toList()));
                }
                """.formatted(names.name(),
                typeNames.boxed()));
    }

    private File friendlyGetter() {
        // nb: `%1$s` references the first argument to formatted
        return mixinContext("""
                List<%2$s> get%1$s() // cleaner getter - drop the -List from getXXXList
                {
                    return get%1$sList();
                }
                """.formatted(names.name(),
                typeNames.boxed()));
    }

    private File streamGetter() {
        // nb: `%1$s` references the first argument to formatted
        return mixinContext("""
                Stream<%2$s> get%1$sStream() // convenient stream accessor 
                {
                    return get%1$sList().stream();
                }
                """.formatted(names.name(),
                typeNames.boxed()));
    }

    @Override
    public GeneratedResponseFileCoordinates context() {
        return fieldContext;
    }
}
