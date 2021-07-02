package com.boeckerman.jake.protobuf;

import com.boeckerman.jake.protobuf.nested.RootModifications;
import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.compiler.PluginProtos;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static com.google.protobuf.compiler.PluginProtos.CodeGeneratorRequest;

public class Plugin {
    public static void main(String[] args) throws IOException {
        //https://developers.google.com/protocol-buffers/docs/overview#customoptions
        ExtensionRegistry registry = ExtensionRegistry.newInstance();
        com.boeckerman.jake.protobuf.Extensions.registerAllExtensions(registry);

        CodeGeneratorRequest request = CodeGeneratorRequest.parseFrom(System.in, registry);
        CodeGenerator generator = new RootModifications(request);

        try {
            PluginProtos.CodeGeneratorResponse response = generator.generate();
            response.writeTo(System.out);
        } catch (Throwable t) {
            StringWriter sw = new StringWriter();
            t.printStackTrace(new PrintWriter(sw));
            PluginProtos.CodeGeneratorResponse.newBuilder()
                    .setError(sw.toString())
                    .build()
                    .writeTo(System.out);

        }
    }
}
