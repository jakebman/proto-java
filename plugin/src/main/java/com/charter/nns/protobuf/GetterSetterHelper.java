package com.charter.nns.protobuf;

import com.google.protobuf.compiler.PluginProtos;

public interface GetterSetterHelper extends FieldHandler {

    // Implementing classes are encouraged to cache this. However, there's no need to require them to do so.
    default NameVariants.FieldNames nameVariants() {
        return new NameVariants.FieldNames(context());
    }

    // needed for the mixin to compile. Both the Builder and the Message already have this defined
    default PluginProtos.CodeGeneratorResponse.File has() {
        return mixinContext("boolean has%s();".formatted(nameVariants().protoMangledName()));
    }

    // needed for the mixin to compile. Both the Builder and the Message already have this defined
    default PluginProtos.CodeGeneratorResponse.File getter() {
        return mixinContext("%s get%s();".formatted(protoType(), nameVariants().protoMangledName()));
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
