package com.boeckerman.jake.protobuf;

import java.io.IOException;

import static com.google.protobuf.compiler.PluginProtos.CodeGeneratorRequest;

public class Plugin {
    public static void main(String[] args) throws IOException {
        CodeGeneratorRequest request = CodeGeneratorRequest.parseFrom(System.in);
        CodeGenerator generator = new CodeGeneratorImpl(request);

        generator.generate().writeTo(System.out);
    }
}
