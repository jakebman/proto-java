package com.boeckerman.jake.protobuf;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.protobuf.DescriptorProtos.*;
import static com.google.protobuf.compiler.PluginProtos.*;
import static com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.*;

public class CodeGeneratorImpl implements CodeGenerator {
    private static final int SHORT_LIST_TO_SEARCH_QUICKLY = 12;
    private final CodeGeneratorRequest request;

    public CodeGeneratorImpl(CodeGeneratorRequest request) {
        this.request = request;
    }

    @Override
    public CodeGeneratorResponse generate() {
        return CodeGeneratorResponse.newBuilder()
                .addAllFile(modifications(request))
                .setSupportedFeatures(Feature.FEATURE_PROTO3_OPTIONAL_VALUE) // Trivial support - we don't care
                .build();
    }


    private List<File> modifications(CodeGeneratorRequest request) {
        return request
                .getFileToGenerateList() // list of .proto file names to work with
                .stream()
                .map(generateLookupMapFor(request.getProtoFileList()))
                .flatMap(this::modifications)
                .collect(Collectors.toList());
    }

    private Function<String, FileDescriptorProto> generateLookupMapFor(List<FileDescriptorProto> protoFileList) {
//       Will never be useful - too many messages files normally (need library files)
//        if(protoFileList.size() < SHORT_LIST_TO_SEARCH_QUICKLY) {
//            return (name) -> {
//                for(FileDescriptorProto p : protoFileList) {
//                    if (p.getName().equals(name)) {
//                        return p;
//                    }
//                }
//                return null;
//            };
//        }
        Map<String, FileDescriptorProto> lookup = new HashMap<>();
        request.getProtoFileList()
                .forEach(fileDescriptorProto -> lookup.put(fileDescriptorProto.getName(), fileDescriptorProto));
        return lookup::get;
    }

    private Stream<File> modifications(FileDescriptorProto fileDescriptorProto) {
        return fileDescriptorProto
                .getMessageTypeList()
                .stream()
                .map(descriptorProto -> addInterface(fileDescriptorProto, descriptorProto));
    }

    private File addInterface(FileDescriptorProto fileDescriptorProto,
                              DescriptorProto descriptorProto) {
        return File.newBuilder()
                .setName(CodeGeneratorUtils.fileToModify(fileDescriptorProto, descriptorProto))
                .setInsertionPoint("message_implements:" + CodeGeneratorUtils.insertionPointTypename(descriptorProto, fileDescriptorProto))
                .setContent("java.io.Serializable, // LOL, I can modify! -Jake")
                .build();
    }
}
