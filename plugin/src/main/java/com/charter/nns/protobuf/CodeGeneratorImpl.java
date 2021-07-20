package com.charter.nns.protobuf;

import com.charter.nns.protobuf.Context.FieldContext;
import com.charter.nns.protobuf.Context.FileContext;
import com.charter.nns.protobuf.Context.MessageContext;
import com.charter.nns.protobuf.Context.RootContext;
import com.charter.nns.protobuf.filecoordinates.CustomMixinFile;
import com.charter.nns.protobuf.filecoordinates.InsertionPoint;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
                .map(CodeGeneratorUtils.fileNameToProtoFileDescriptorLookup(context.request().getProtoFileList()))
                .map(context::withFile)
                .flatMap(this::modifications)
                .collect(Collectors.toList());
    }

    private Stream<File> modifications(FileContext fileContext) {
        return fileContext.fileDescriptorProto()
                .getMessageTypeList()
                .stream()
                .map(fileContext::withMessage)
                .flatMap(this::modifications);
    }

    private Stream<File> modifications(MessageContext messageContext) {
        if (messageContext.javaMessageExtensions().getEnabled()) {
            return StreamUtil.concat(
                    addMarkerInterfaceAndComment(messageContext),
                    addImplementedInterfaces(messageContext),
                    messageContext.descriptorProto()
                            .getFieldList()
                            .stream()
                            .map(messageContext::withFieldDescriptor)
                            .flatMap(this::modifications)
                            .mapMulti(CustomMixinFile.alsoEmitMixinFileWhenNeeded(messageContext)));
        }
        return Stream.empty();
    }

    private Stream<File> addImplementedInterfaces(MessageContext messageContext) {
        return StreamUtil.<File>concat(
                messageContext.javaMessageExtensions()
                        .getImplementsList()
                        .stream()
                        .map(intface -> messageContext.fileBuilderFor(InsertionPoint.message_implements)
                                .setContent(intface + ", // added by protoc extension")
                                .build()),
                messageContext.javaMessageExtensions()
                        .getBuilderImplementsList()
                        .stream()
                        .map(intface -> messageContext.fileBuilderFor(InsertionPoint.builder_implements)
                                .setContent(intface + ", // added by protoc extension")
                                .build()),
                messageContext.javaMessageExtensions()
                        .getMessageOrBuilderImplementsList()
                        .stream()
                        .map(intface -> messageContext.fileBuilderFor(InsertionPoint.interface_extends)
                                .setContent(intface + ", // added by protoc extension")
                                .build()));
    }

    private File addMarkerInterfaceAndComment(MessageContext messageContext) {
        return messageContext.fileBuilderFor(InsertionPoint.message_implements)
                .setContent(Touched.class.getName() + ", //" + Touched.MESSAGE)
                .build();
    }

    private Stream<File> modifications(FieldContext fieldContext) {
        if (fieldContext.fieldExtension().getEnabled()) {
            return StreamUtil.concat(
                    new NullableFields(fieldContext),
                    new ListFields(fieldContext),
                    new AliasFields(fieldContext),
                    new InlineFieldsOrMethodsOrWhateverReally(fieldContext)
            );
        } else {
            return Stream.empty();
        }
    }
}
