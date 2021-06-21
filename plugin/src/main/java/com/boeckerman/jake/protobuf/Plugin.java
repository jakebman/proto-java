package com.boeckerman.jake.protobuf;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.compiler.PluginProtos;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Plugin {
    public static void main(String[] args) throws IOException {
        PluginProtos.CodeGeneratorRequest request = PluginProtos.CodeGeneratorRequest.parseFrom(System.in);

        PluginProtos.CodeGeneratorResponse
                .newBuilder()
                .addAllFile(modifications(request))
                .build()
                .writeTo(System.out);

    }

    private static List<PluginProtos.CodeGeneratorResponse.File> modifications(PluginProtos.CodeGeneratorRequest request) {
        return request
                .getProtoFileList()
                .stream()
                .flatMap(fileDescriptorProto -> modifications(fileDescriptorProto, request))
                .collect(Collectors.toList());
    }

    private static Stream<PluginProtos.CodeGeneratorResponse.File> modifications(DescriptorProtos.FileDescriptorProto fileDescriptorProto, PluginProtos.CodeGeneratorRequest request) {
        return fileDescriptorProto.getMessageTypeList().stream().map(Plugin::addInterface);
    }

    private static PluginProtos.CodeGeneratorResponse.File addInterface(DescriptorProtos.DescriptorProto descriptorProto) {
        return PluginProtos.CodeGeneratorResponse.File.newBuilder()
                .setName(descriptorProto.getName())
                .setContent("// LOL, I can modify! -Jake")
                .build();
    }

}
