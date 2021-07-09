package com.boeckerman.jake.protobuf;

import com.google.protobuf.compiler.PluginProtos;

public interface GetterSetterHelper extends FieldHandler {
    NameVariants.FieldNames nameVariants();

    TypeUtils.TypeNames typeNames();

    // needed for the mixin to compile. Both the Builder and the Message already have this defined
    default PluginProtos.CodeGeneratorResponse.File has() {
        return mixinContext(existingMethodDeclaration("boolean", "has"));
    }

    // needed for the mixin to compile. Both the Builder and the Message already have this defined
    default PluginProtos.CodeGeneratorResponse.File getter() {
        return mixinContext(existingMethodDeclaration(protoType(), "get"));
    }

    default String existingMethodDeclaration(String type, String verb) {
        return methodDeclarationHeader(type, verb, nameVariants().protoGeneratedName()).append(";").toString();
    }

    default String protoType() {
        return typeNames().primitive();
    }

    default String nullableType() {
        return typeNames().boxed();
    }
}
