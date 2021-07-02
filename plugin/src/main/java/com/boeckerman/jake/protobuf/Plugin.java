package com.boeckerman.jake.protobuf;

import com.google.protobuf.ExtensionRegistry;

import java.io.IOException;

import static com.google.protobuf.compiler.PluginProtos.CodeGeneratorRequest;

public class Plugin {
    public static void main(String[] args) throws IOException {
        //https://developers.google.com/protocol-buffers/docs/overview#customoptions
        ExtensionRegistry registry = ExtensionRegistry.newInstance();
        com.boeckerman.jake.protobuf.Extensions.registerAllExtensions(registry);

        CodeGeneratorRequest request = CodeGeneratorRequest.parseFrom(System.in, registry);
        CodeGenerator generator = new CodeGeneratorImpl(request);

        generator.generate().writeTo(System.out);
    }
}
