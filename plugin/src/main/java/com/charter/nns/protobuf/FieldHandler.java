package com.charter.nns.protobuf;

import com.charter.nns.protobuf.filecoordinates.InsertionPoint;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.File;

import java.util.function.Supplier;
import java.util.stream.Stream;

public interface FieldHandler extends Supplier<Stream<File>> {

    Context.FieldContext context();

    static boolean isList(Context.FieldContext context) {
        return context.fieldDescriptorProto().getLabel() == DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED;
    }

    default boolean isList() {
        return isList(context());
    }

    default TypeUtils.TypeNames typeNames() {
        return context().executionContext().typeNames().lookup(context().fieldDescriptorProto());
    }

    default boolean isMap() {
        return typeNames().descriptorProtoDefault().getOptions().getMapEntry();
    }


    default File mixinContext(String content) {
        return context()
                .fileBuilderFor(InsertionPoint.custom_mixin_interface_scope)
                .setContent(content)
                .build();
    }

    default File builderContext(String content) {
        return context()
                .fileBuilderFor(InsertionPoint.builder_scope)
                .setContent(content)
                .build();
    }

    default File classContext(String content) {
        return context()
                .fileBuilderFor(InsertionPoint.class_scope)
                .setContent(content)
                .build();
    }

    default Stream<File> warningResponse(String content) {
        return Stream.of(mixinContext(content));
    }
}
