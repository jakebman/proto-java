package com.charter.nns.protobuf;

import com.charter.nns.protobuf.filecoordinates.GeneratedResponseFileCoordinates;
import com.charter.nns.protobuf.filecoordinates.InsertionPoint;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.File;

import java.util.function.Supplier;
import java.util.stream.Stream;

public interface FieldHandler extends Supplier<Stream<File>> {

    GeneratedResponseFileCoordinates context();

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

    default Stream<File> warningResponse(String content) {
        return Stream.of(mixinContext(content));
    }


    // String getFoo()
    // Object flyBar(String one, String two)
    default StringBuilder methodDeclarationHeader(Object type, String verb, String fieldName, String... args) {
        StringBuilder out = new StringBuilder();
        out.append(type);
        out.append(" ");
        out.append("%s%s(%s)".formatted(verb, fieldName, (String.join(",", args))));
        return out;
    }
}
