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

    default String existingMethodDeclaration(String type, String verb) {
        if (isList()) {
            return methodDeclarationHeader(type, verb, nameVariants().protoGeneratedName() + "List").append(";").toString();
        } else {
            return methodDeclarationHeader(type, verb, nameVariants().protoGeneratedName()).append(";").toString();
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
