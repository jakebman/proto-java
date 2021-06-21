package com.boeckerman.jake.protobuf;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.protobuf.DescriptorProtos.*;
import static com.google.protobuf.compiler.PluginProtos.*;
import static com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.*;

public class Plugin {
    public static void main(String[] args) throws IOException {
        CodeGeneratorRequest request = CodeGeneratorRequest.parseFrom(System.in);

        CodeGeneratorResponse.newBuilder() // Intellij wants to hide reference to CodeGeneratorResponse here
                .addAllFile(modifications(request))
                .build()
                .writeTo(System.out);

    }

    private static List<File> modifications(CodeGeneratorRequest request) {
        Map<String, FileDescriptorProto> lookup = new HashMap<>();
        request.getProtoFileList()
                .forEach(fileDescriptorProto -> lookup.put(fileDescriptorProto.getName(), fileDescriptorProto));
        return request
                .getFileToGenerateList()
                .stream()
                .map(lookup::get)
                .flatMap(Plugin::modifications)
                .collect(Collectors.toList());
    }

    private static Stream<File> modifications(FileDescriptorProto fileDescriptorProto) {
        return fileDescriptorProto
                .getMessageTypeList()
                .stream()
                .map(descriptorProto -> addInterface(fileDescriptorProto, descriptorProto));
    }

    private static File addInterface(FileDescriptorProto fileDescriptorProto,
                                     DescriptorProto descriptorProto) {
        return File.newBuilder()
                .setName(fileToModify(fileDescriptorProto, descriptorProto))
                .setInsertionPoint("message_implements:" + getInsertionPointFor(descriptorProto, fileDescriptorProto))
                .setContent("java.io.Serializable, // LOL, I can modify! -Jake")
                .build();
    }

    private static String getInsertionPointFor(DescriptorProto descriptorProto,
                                               FileDescriptorProto fileDescriptorProto) {
        if(fileDescriptorProto.hasPackage()){
            return fileDescriptorProto.getPackage() + "." + descriptorProto.getName();
        }
        return descriptorProto.getName();
    }

    /* TODO: how do we behave correctly in the following situations
     * No java package
     * Java wrapper class?
     */
    private static String fileToModify(FileDescriptorProto fileDescriptorProto,
                                       DescriptorProto descriptorProto) {

        FileOptions options = fileDescriptorProto.getOptions();
        StringBuilder out = new StringBuilder();
        if (options.hasJavaPackage()) {
            out.append(packageToPath(options.getJavaPackage()));
        } else if (fileDescriptorProto.hasPackage()) {
            out.append(packageToPath(fileDescriptorProto.getPackage()));
        }
        if(out.length() > 0) {
            out.append("/");
        }
        out.append(descriptorProto.getName())
                .append(".java");
        return out.toString();
    }

    private static final Pattern singleDot = Pattern.compile("\\.");
    private static String packageToPath(String javaPackage) {
        return singleDot.matcher(javaPackage).replaceAll("/");
    }

}
