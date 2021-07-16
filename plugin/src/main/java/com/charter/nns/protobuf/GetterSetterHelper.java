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
        return mixinContext(existingMethodDeclaration("boolean", "has"));
    }

    // needed for the mixin to compile. Both the Builder and the Message already have this defined
    default PluginProtos.CodeGeneratorResponse.File getter() {
        return mixinContext(existingMethodDeclaration(protoType(), "get"));
    }

    String LIST_GETTER_HASER_DECLARATION = "%s %s%sList();";
    String GETTER_HASER_DECLARATION = "%s %s%s();";
    default String existingMethodDeclaration(String type, String verb) {
        if (isList()) {
            return (LIST_GETTER_HASER_DECLARATION.formatted(type, verb, nameVariants().protoGeneratedName()));
        } else {
            return (GETTER_HASER_DECLARATION.formatted(type, verb, nameVariants().protoGeneratedName()));
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
