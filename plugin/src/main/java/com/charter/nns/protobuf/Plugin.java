package com.charter.nns.protobuf;

import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.compiler.PluginProtos;
import com.google.protobuf.util.JsonFormat;

import java.io.FileWriter;
import java.io.IOException;

import static com.google.protobuf.compiler.PluginProtos.CodeGeneratorRequest;

public class Plugin {

    public static void main(String[] args) throws IOException {
        //https://developers.google.com/protocol-buffers/docs/overview#customoptions
        ExtensionRegistry registry = ExtensionRegistry.newInstance();
        Extensions.registerAllExtensions(registry);

        CodeGeneratorRequest request = CodeGeneratorRequest.parseFrom(System.in, registry);
        if (args.length > 0) {
            request = request
                    .toBuilder()
                    .setParameter(String.join(",", args))
                    .build();
        }

        CodeGenerator generator = new CodeGeneratorImpl();

        PluginProtos.CodeGeneratorResponse response = generator.generate(request);
        writeDebug(request, response);
        response.writeTo(System.out);
    }

    private static void writeDebug(CodeGeneratorRequest request, PluginProtos.CodeGeneratorResponse response) throws IOException {
        if (!request.getParameter().contains(Context.DEBUG)) {
            return;
        }
        RequestResponse.Builder message = RequestResponse.newBuilder().setRequest(request).setResponse(response);
        if (!request.getParameter().contains(Context.DEBUG_VERBOSE)) {
            message.setRequest(CodeGeneratorUtils.filter(request));
        }

        // Auto-close the FileWriter
        try (FileWriter output = new FileWriter("debug-output.json")) {
            JsonFormat.printer().appendTo(message, output);
        }
    }
}
