package com.charter.nns.protobuf;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.compiler.PluginProtos;

public interface GetterSetterHelper extends FieldHandler {
    @Override
    Context.FieldContext context();

    default NameVariants.FieldNames nameVariants() {
        return new NameVariants.FieldNames(context());
    }

    default TypeUtils.TypeNames typeNames() {
        return context().executionContext().typeNames().lookup(context().fieldDescriptorProto());
    }

    default boolean isList() {
        return context().fieldDescriptorProto().getLabel() == DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED;
    }

    default boolean isMap() {
        return typeNames().descriptorProtoDefault().getOptions().getMapEntry();
    }

    // needed for the mixin to compile. Both the Builder and the Message already have this defined
    default PluginProtos.CodeGeneratorResponse.File has() {
        if (isList()) {
            return mixinContext("boolean has%sList();".formatted(nameVariants().protoGeneratedName()));
        } else {
            return mixinContext("boolean has%s();".formatted(nameVariants().protoGeneratedName()));
        }
    }

    // needed for the mixin to compile. Both the Builder and the Message already have this defined
    default PluginProtos.CodeGeneratorResponse.File getter() {
        if (isList()) {
            return mixinContext("%s get%sList();".formatted(protoType(), nameVariants().protoGeneratedName()));
        } else {
            return mixinContext("%s get%s();".formatted(protoType(), nameVariants().protoGeneratedName()));
        }
    }

    default String protoType() {
        if (isList()) {
            return ListFields.listOf(typeNames().boxed());
        } else {
            return typeNames().primitive();
        }
    }

    default String nullableType() {
        return typeNames().boxed();
    }
}
