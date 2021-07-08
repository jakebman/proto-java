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
        this.typeNames = typeReference.lookup(fieldDescriptorProto);
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
        if (listOptions.getFriendlyGetter() || listOptions.getStreamGetter()) {
            builder.add(getter());
            if (listOptions.getFriendlyGetter()) {
                builder.add(friendlyGetter());
            }
            if (listOptions.getStreamGetter()) {
                builder.add(streamGetter());
            }
        }
        return builder.build();
    }

    // needed for the mixin to compile. Both the Builder and the Message already have this defined
    private File getter() {
        return mixinContext(methodDeclarationHeader(listOf(typeNames.boxed()), "get", names.name()).append(";").toString());
    }

    private String listOf(String boxed) {
        return "java.util.List<%s>".formatted(boxed);
    }


    private File addAllAcceptsStream() {
        // nb: `%1$s` references the first argument to formatted
        return builderContext("""
                Builder addAll%1$s(java.util.stream.Stream<%2$s> value) // Stream-friendly addAll
                {
                    return addAll%1$s(value.collect(java.util.stream.Collectors.toList()));
                }
                """.formatted(names.name(),
                typeNames.boxed()));
    }

    private File friendlyGetter() {
        // nb: `%1$s` references the first argument to formatted
        return mixinContext("""
                default java.util.List<%2$s> get%1$s() // cleaner getter - drop the -List from getXXXList
                {
                    return get%1$sList();
                }
                """.formatted(names.name(),
                typeNames.boxed()));
    }

    private File streamGetter() {
        // nb: `%1$s` references the first argument to formatted
        return mixinContext("""
                default java.util.stream.Stream<%2$s> get%1$sStream() // convenient stream accessor
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
