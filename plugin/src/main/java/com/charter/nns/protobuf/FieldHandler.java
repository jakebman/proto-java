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
}
