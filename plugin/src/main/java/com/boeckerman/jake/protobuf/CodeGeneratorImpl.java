package com.boeckerman.jake.protobuf;

import com.boeckerman.jake.protobuf.Context.*;
import com.boeckerman.jake.protobuf.Extensions.JavaExtensionOptions;
import com.boeckerman.jake.protobuf.Extensions.JavaExtensionOptions.NullableOptionsOrBuilder;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Label.LABEL_OPTIONAL;
import static com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import static com.google.protobuf.DescriptorProtos.MessageOptions;
import static com.google.protobuf.compiler.PluginProtos.CodeGeneratorRequest;
import static com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse;
import static com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.Feature;
import static com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.File;

public class CodeGeneratorImpl implements CodeGenerator {

    @Override
    public CodeGeneratorResponse generate(CodeGeneratorRequest request) {
        RootContext context = new RootContext(request);
        return CodeGeneratorResponse.newBuilder()
                .addAllFile(modifications(context))
                .setSupportedFeatures(Feature.FEATURE_PROTO3_OPTIONAL_VALUE) // anticipate emphatic support - we want this
                .build();
    }

    private List<File> modifications(RootContext context) {
        return context.request()
                .getFileToGenerateList() // list of .proto file names to work with
                .stream()
                .map(fileNameToProtoFileDescriptorLookup(context.request().getProtoFileList()))
                .map(context::withFile)
                .flatMap(this::modifications)
                .collect(Collectors.toList());
    }

    private Function<String, FileDescriptorProto> fileNameToProtoFileDescriptorLookup(List<FileDescriptorProto> protoFileList) {
        Map<String, FileDescriptorProto> lookup = protoFileList.stream()
                .collect(Collectors.toMap(FileDescriptorProto::getName, Function.identity()));
        return lookup::get;
    }

    private Stream<File> modifications(FileContext fileContext) {
        return fileContext.fileDescriptorProto()
                .getMessageTypeList()
                .stream()
                .map(fileContext::withMessage)
                .flatMap(this::modifications);
    }

    private Stream<File> modifications(MessageContext messageContext) {
        return StreamUtil.concat(
                addInterfaceComment(messageContext), // nb: returns a single File, but that's not super relevant, which is why concat has an overload
                applyNullableOptions(messageContext));
    }

    private Stream<File> applyNullableOptions(MessageContext messageContext) {
        if (!messageContext.javaExtensionOptions().getNullableOptionals().getEnabled()) {
            return Stream.empty();
        }
        return messageContext.descriptorProto().getFieldList()
                .stream()
                .filter(f -> f.getLabel() == LABEL_OPTIONAL)
                .map(messageContext::withField)
                .flatMap(this::applyNullableOptions);
    }

    private Stream<File> applyNullableOptions(FieldContext fieldContext) {
        NullableOptionsOrBuilder nullableOptionals = fieldContext.javaExtensionOptions().getNullableOptionals();
        if (CodeGeneratorUtils.isPrimitive(fieldContext.fieldDescriptorProto().getType()) && fieldContext.fieldDescriptorProto().getName().endsWith(nullableOptionals.getPrimitiveSuffix())) {
            return Stream.of(fieldContext.fileBuilderFor(InsertionPoint.class_scope)
                    .setContent("//" + this.getClass().getName() + " - Recognize primitive we need to work on " + fieldContext.fieldDescriptorProto().getName())
                    .build());
        } else if (!CodeGeneratorUtils.isPrimitive(fieldContext.fieldDescriptorProto().getType()) && fieldContext.fieldDescriptorProto().getName().endsWith(nullableOptionals.getObjectSuffix())) {
            return Stream.of(fieldContext.fileBuilderFor(InsertionPoint.class_scope)
                    .setContent("//" + this.getClass().getName() + " - Recognize Object we need to work on " + fieldContext.fieldDescriptorProto().getName())
                    .build());
        } else {
            return Stream.empty();
        }
    }

    private Stream<File> addInterfaceCommentIfOptionsEnabled(MessageContext messageContext) {
        MessageOptions options = messageContext.descriptorProto().getOptions();
        if (!options.hasExtension(Extensions.javaHelper)) {
            return Stream.empty();
        }
        JavaExtensionOptions extension = options.getExtension(Extensions.javaHelper);
        if (extension.getEnabled()) {
            return Stream.of(addInterfaceComment(messageContext));
        } else {
            return Stream.empty();
        }
    }

    private File addInterfaceComment(MessageContext messageContext) {
        return InsertionPoint.message_implements.fileBuilderFor(messageContext)
                .setContent("// Marker Comment: this class has opted in to boeckerman.jake.protobuf.java_helper")
                .build();
    }
}
