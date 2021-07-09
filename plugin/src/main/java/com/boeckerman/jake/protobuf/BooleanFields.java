package com.boeckerman.jake.protobuf;

import com.boeckerman.jake.protobuf.filecoordinates.GeneratedResponseFileCoordinates;
import com.google.protobuf.compiler.PluginProtos;

import java.util.stream.Stream;

public class BooleanFields implements FieldHandler {
    private final Context.FieldContext fieldContext;

    public BooleanFields(Context.FieldContext fieldContext) {
        this.fieldContext = fieldContext;
    }

    @Override
    public GeneratedResponseFileCoordinates context() {
        return fieldContext;
    }

    @Override
    public Stream<PluginProtos.CodeGeneratorResponse.File> get() {
        // TODO: implement me
        return Stream.empty();
    }
}
