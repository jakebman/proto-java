package com.charter.nns.protobuf;

import com.charter.nns.protobuf.filecoordinates.GeneratedResponseFileCoordinates;
import com.charter.nns.protobuf.filecoordinates.InsertionPoint;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.File;

import java.util.Arrays;
import java.util.function.Supplier;
import java.util.stream.Collectors;
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
        methodInvoke(verb, fieldName, out, args);
        return out;
    }

    default StringBuilder methodInvoke(String verb, String fieldName, String... args) {
        return methodInvoke(verb, fieldName, new StringBuilder(), args);
    }

    // flyBar(String one, String two)
    // OR
    // setBar(one, two)
    default StringBuilder methodInvoke(String verb, String fieldName, StringBuilder out, String... args) {
        return methodName(verb, fieldName, out)
                .append(Arrays.stream(args).collect(Collectors.joining(",", "(", ")")));
    }

    default StringBuilder methodName(String verb, String fieldName, StringBuilder out) {
        out.append(verb);
        out.append(fieldName);
        return out;
    }
}
