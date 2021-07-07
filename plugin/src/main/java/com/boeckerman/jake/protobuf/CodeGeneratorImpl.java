package com.boeckerman.jake.protobuf;

import com.boeckerman.jake.protobuf.Context.FieldContext;
import com.boeckerman.jake.protobuf.Context.FileContext;
import com.boeckerman.jake.protobuf.Context.MessageContext;
import com.boeckerman.jake.protobuf.Context.RootContext;
import com.boeckerman.jake.protobuf.Extensions.JavaFieldExtension;
import com.boeckerman.jake.protobuf.Extensions.JavaFieldExtension.ListOptions;
import com.boeckerman.jake.protobuf.Extensions.JavaFieldExtension.NullableOptions;
import com.boeckerman.jake.protobuf.filecoordinates.CustomMixinFile;
import com.boeckerman.jake.protobuf.filecoordinates.InsertionPoint;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;

import java.util.List;
 import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Label.LABEL_OPTIONAL;
import static com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED;
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
                    messageContext.descriptorProto()
                            .getFieldList()
                            .stream()
                            .map(messageContext::withFieldDescriptor)
                            .flatMap(this::modifications)
                            .mapMulti(CustomMixinFile.alsoEmitMixinFileWhenNeeded(messageContext)));
        }
        return Stream.empty();
    }

    private File addMarkerInterfaceAndComment(MessageContext messageContext) {
        return messageContext.fileBuilderFor(InsertionPoint.message_implements)
                .setContent(Touched.class.getName() + ", //" + Touched.MESSAGE)
                .build();
    }

    private Stream<File> modifications(FieldContext fieldContext) {
        if (fieldContext.fieldExtension().getEnabled()) {
            return StreamUtil.concat(
                    applyNullableOptions(fieldContext),
                    applyListOptions(fieldContext),
                    applyAliasOptions(fieldContext),
                    applyBooleanOptions(fieldContext)
            );
        } else {
            return Stream.empty();
        }
    }

    private Stream<File> applyNullableOptions(FieldContext fieldContext) {
        JavaFieldExtension javaFieldExtension = fieldContext.fieldExtension();
        FieldDescriptorProto fieldDescriptorProto = fieldContext.fieldDescriptorProto();
        if (fieldDescriptorProto.getLabel() != LABEL_OPTIONAL) {
            return Stream.empty();
        }
        NullableOptions nullableOptionals = javaFieldExtension.getNullable();
        if (CodeGeneratorUtils.isPrimitive(fieldDescriptorProto.getType())
                && fieldDescriptorProto.getName().endsWith(nullableOptionals.getPrimitiveSuffix())) {

            return Stream.of(fieldContext.fileBuilderFor(InsertionPoint.custom_mixin_interface_scope)
                    .setContent("//" + this.getClass().getName() + " - Recognize primitive we need to work on " + fieldDescriptorProto.getName())
                    .build());
        } else if (!CodeGeneratorUtils.isPrimitive(fieldDescriptorProto.getType())
                && fieldDescriptorProto.getName().endsWith(nullableOptionals.getObjectSuffix())) {

            return Stream.of(fieldContext.fileBuilderFor(InsertionPoint.custom_mixin_interface_scope)
                    .setContent("//" + this.getClass().getName() + " - Recognize Object we need to work on " + fieldDescriptorProto.getName())
                    .build());
        } else {
            return Stream.empty();
        }
    }

    private Stream<File> applyListOptions(FieldContext fieldContext) {
        JavaFieldExtension javaFieldExtension = fieldContext.fieldExtension();
        FieldDescriptorProto fieldDescriptorProto = fieldContext.fieldDescriptorProto();
        if (fieldDescriptorProto.getLabel() != LABEL_REPEATED) {
            return Stream.empty();
        }
        ListOptions listOptions = javaFieldExtension.getList();
        //Stub.
        return Stream.empty();
    }

    private Stream<File> applyAliasOptions(FieldContext fieldContext) {
        // Stub.
        return Stream.empty();
    }

    private Stream<File> applyBooleanOptions(FieldContext fieldContext) {
        // Stub.
        return Stream.empty();
    }
}
