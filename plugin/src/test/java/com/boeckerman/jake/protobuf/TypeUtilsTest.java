package com.boeckerman.jake.protobuf;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.compiler.PluginProtos;
import org.junit.Test;

import java.util.Arrays;

import static com.boeckerman.jake.protobuf.CodeGeneratorUtils.CamelCase;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import static org.junit.Assert.assertThat;
public class TypeUtilsTest {

    public static final String JAVA_PACKAGE = "some.java.package.string";
    public static final String MESSAGE_NAME = "aMessageName";

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
                is(JAVA_PACKAGE + TypeUtils.PACKAGE_SEPERATOR + CamelCase(MESSAGE_NAME)));
    }
}