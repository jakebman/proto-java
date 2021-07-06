package com.boeckerman.jake.protobuf;

import com.boeckerman.jake.protobuf.Extensions.JavaExtensionOptions.NullableOptions;
import com.boeckerman.jake.protobuf.domains.Types;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.protobuf.DescriptorProtos.*;
import static com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Label.LABEL_OPTIONAL;
import static com.google.protobuf.compiler.PluginProtos.CodeGeneratorRequest;
import static com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse;
import static com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.Feature;
import static com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.File;

public class CodeGeneratorImpl implements CodeGenerator {
    private final CodeGeneratorRequest request;

    public CodeGeneratorImpl(CodeGeneratorRequest request) {
        this.request = request;
    }

    @Override
    public CodeGeneratorResponse generate() {
        return CodeGeneratorResponse.newBuilder()
                .addAllFile(modifications(request))
                .setSupportedFeatures(Feature.FEATURE_PROTO3_OPTIONAL_VALUE) // anticipate emphatic support - we want this
                .build();
    }


    private List<File> modifications(CodeGeneratorRequest request) {
        return request
                .getFileToGenerateList() // list of .proto file names to work with
                .stream()
                .map(fileNameToProtoFileDescriptorLookup(request.getProtoFileList()))
                .flatMap(this::modifications)
                .collect(Collectors.toList());
    }

    private Function<String, FileDescriptorProto> fileNameToProtoFileDescriptorLookup(List<FileDescriptorProto> protoFileList) {
        Map<String, FileDescriptorProto> lookup = protoFileList.stream()
                .collect(Collectors.toMap(FileDescriptorProto::getName, Function.identity()));
        return lookup::get;
    }

    private Stream<File> modifications(FileDescriptorProto fileDescriptorProto) {
        return fileDescriptorProto
                .getMessageTypeList()
                .stream()
                .flatMap(descriptorProto -> modifications(fileDescriptorProto, descriptorProto));
    }

    private Stream<File> modifications(FileDescriptorProto fileDescriptorProto, DescriptorProto descriptorProto) {
        MessageOptions options = descriptorProto.getOptions();
        if(!options.hasExtension(Extensions.javaHelper)) {
            return Stream.empty();
        }
        Extensions.JavaExtensionOptions extension = options.getExtension(Extensions.javaHelper);
        if (extension.getEnabled()) {
            return Stream.of(
                    Stream.of(addInterfaceComment(fileDescriptorProto, descriptorProto)),
                    applyNullableOptions(extension.getNullableOptionals(), fileDescriptorProto, descriptorProto)
            )
                    .flatMap(Function.identity());
        } else {
            return Stream.empty();
        }
    }

    private Stream<File> applyNullableOptions(NullableOptions nullableOptionals, FileDescriptorProto fileDescriptorProto, DescriptorProto descriptorProto) {
        if(!nullableOptionals.getEnabled()) {
            return Stream.empty();
        }
        return descriptorProto.getFieldList()
                .stream()
                .filter(f -> f.getLabel()==LABEL_OPTIONAL)
                .flatMap(f -> applyNullableOptions(nullableOptionals, fileDescriptorProto, descriptorProto, f));
    }

    private Stream<File> applyNullableOptions(NullableOptions nullableOptionals,
                                              FileDescriptorProto fileDescriptorProto,
                                              DescriptorProto descriptorProto,
                                              FieldDescriptorProto fieldDescriptorProto) {
        if(Types.isPrimitive(fieldDescriptorProto.getType()) && fieldDescriptorProto.getName().endsWith(nullableOptionals.getPrimitiveSuffix())){
            return Stream.of(InsertionPoint.InsertionPointPrefix.class_scope.fileBuilderFor(fileDescriptorProto, descriptorProto)
                    .setContent("//"+ this.getClass().getName() + " - Recognize primitive we need to work on " + fieldDescriptorProto.getName())
                    .build());
        } else if(!Types.isPrimitive(fieldDescriptorProto.getType()) && fieldDescriptorProto.getName().endsWith(nullableOptionals.getObjectSuffix())){
            return Stream.of(InsertionPoint.InsertionPointPrefix.class_scope.fileBuilderFor(fileDescriptorProto, descriptorProto)
                    .setContent("//"+ this.getClass().getName() + " - Recognize Object we need to work on " + fieldDescriptorProto.getName())
                    .build());
        } else {
            return Stream.empty();
        }
    }

    private Stream<File> addInterfaceCommentIfOptionsEnabled(FileDescriptorProto fileDescriptorProto,
                                                             DescriptorProto descriptorProto) {
        MessageOptions options = descriptorProto.getOptions();
        if(!options.hasExtension(Extensions.javaHelper)) {
            return Stream.empty();
        }
        Extensions.JavaExtensionOptions extension = options.getExtension(Extensions.javaHelper);
        if (extension.getEnabled()) {
            return Stream.of(addInterfaceComment(fileDescriptorProto, descriptorProto));
        }else {
            return Stream.empty();
        }
    }
    private File addInterfaceComment(FileDescriptorProto fileDescriptorProto,
                                     DescriptorProto descriptorProto) {
        return InsertionPoint.InsertionPointPrefix.message_implements.fileBuilderFor(fileDescriptorProto, descriptorProto)
                .setContent("// Marker Comment: this class has opted in to boeckerman.jake.protobuf.java_helper")
                .build();
    }

}
