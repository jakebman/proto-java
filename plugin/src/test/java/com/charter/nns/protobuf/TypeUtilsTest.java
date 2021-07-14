package com.charter.nns.protobuf;

import com.charter.nns.protobuf.filecoordinates.GeneratedResponseFileCoordinates;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.compiler.PluginProtos;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class TypeUtilsTest {

    public static final String JAVA_PACKAGE = "some.java.package.string";
    public static final String MESSAGE_NAME = "aMessageName";
    public static final String PROTO_PACKAGE = "some.proto.package";

    public static Stream<GeneratedResponseFileCoordinates> allNestedTypes(Context.FileContext fileContext) {
        return fileContext.fileDescriptorProto()
                .getMessageTypeList()
                .stream()
                .map(fileContext::withMessage)
                .flatMap(TypeUtilsTest::allNestedTypes);
    }

    public static Stream<GeneratedResponseFileCoordinates> allNestedTypes(GeneratedResponseFileCoordinates messageContext) {
        DescriptorProtos.DescriptorProto descriptorProto = messageContext.descriptorProto();
        return StreamUtil.concat(
                messageContext,
                descriptorProto
                        .getNestedTypeList()
                        .stream()
                        .map(messageContext::childFor)
                        .flatMap(TypeUtilsTest::allNestedTypes), // recursion mean never having to say you're sorry.
                descriptorProto
                        .getEnumTypeList()
                        .stream()
                        .map(messageContext::childFor)
                        .flatMap(TypeUtilsTest::allNestedTypes)); // enums do not have nested child messages, but this recursion does not hurt
    }

    @Test
    public void simpleLookupTable() {
        TypeUtils.generateLookupTableFor(PluginProtos.CodeGeneratorRequest.getDefaultInstance());
    }

    @Test
    public void lookupTable() {
        TypeUtils.TypeReference stringJavaTypeNamesMap = TypeUtils.generateLookupTableFor(Arrays.asList(
                DescriptorProtos.FileDescriptorProto.newBuilder()
                        .setOptions(DescriptorProtos.FileOptions.newBuilder()
                                .setJavaPackage(JAVA_PACKAGE)
                                .build())
                        .addMessageType(DescriptorProtos.DescriptorProto.newBuilder()
                                .setName(MESSAGE_NAME)
                                .build()).build()
        ));
        System.out.println(stringJavaTypeNamesMap.describe());
        assertThat(stringJavaTypeNamesMap.lookupMessageType(MESSAGE_NAME).boxed(),
                is(JAVA_PACKAGE + TypeUtils.PACKAGE_SEPERATOR + MESSAGE_NAME));
    }

    @Test
    public void fullnamesWork() {
        Context.RootContext root = new Context.RootContext(nested());

        List<GeneratedResponseFileCoordinates> generatedResponseFileCoordinatesStream = root.request()
                .getFileToGenerateList() // list of .proto file names to work with
                .stream()
                .map(CodeGeneratorUtils.fileNameToProtoFileDescriptorLookup(root.request().getProtoFileList()))
                .map(root::withFile)
                .flatMap(TypeUtilsTest::allNestedTypes).collect(Collectors.toList());
        List<String> fullClassNames = generatedResponseFileCoordinatesStream.stream().map(TypeUtils::javaFullClassName).collect(Collectors.toList());
        System.out.println(fullClassNames);
        assertThat(fullClassNames, hasItem(JAVA_PACKAGE + TypeUtils.PACKAGE_SEPERATOR + "outest.Middle.InnerMost"));

        List<String> protoNames = generatedResponseFileCoordinatesStream.stream().map(TypeUtils::protoFullTypeName).map(Object::toString).collect(Collectors.toList());
        System.out.println(protoNames);
        assertThat(protoNames, hasItem(PROTO_PACKAGE + TypeUtils.PACKAGE_SEPERATOR + "outest.Middle.InnerMost"));
        // assertThat(s, is(("some.java.package.outest.Middle.InnerMost")));

    }

    private PluginProtos.CodeGeneratorRequest nested() {

        DescriptorProtos.DescriptorProto message = DescriptorProtos.DescriptorProto.newBuilder()
                .setName("outest")
                .addNestedType(DescriptorProtos.DescriptorProto.newBuilder()
                        .setName("Middle")
                        .addNestedType(DescriptorProtos.DescriptorProto.newBuilder()
                                .setName("InnerMost")
                                .build())
                        .build())
                .build();
        DescriptorProtos.FileDescriptorProto file = DescriptorProtos.FileDescriptorProto.newBuilder()
                .setName("some_proto-file.proto")
                .setPackage(PROTO_PACKAGE)
                .setOptions(DescriptorProtos.FileOptions.newBuilder()
                        .setJavaPackage(JAVA_PACKAGE)
                        .setJavaMultipleFiles(true)
                        .build())
                .addMessageType(message)
                .build();
        return PluginProtos.CodeGeneratorRequest.newBuilder()
                .addProtoFile(file)
                .addFileToGenerate(file.getName())
                .build();
    }
}