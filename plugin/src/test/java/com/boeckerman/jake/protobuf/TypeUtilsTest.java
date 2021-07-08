package com.boeckerman.jake.protobuf;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.compiler.PluginProtos;
import org.junit.Test;

import java.util.Arrays;
import java.util.Map;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import static com.boeckerman.jake.protobuf.CodeGeneratorUtils.CamelCase;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

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
        Map<String, TypeUtils.JavaTypeNames> stringJavaTypeNamesMap = TypeUtils.generateLookupTableFor(Arrays.asList(
                DescriptorProtos.FileDescriptorProto.newBuilder()
                        .setOptions(DescriptorProtos.FileOptions.newBuilder()
                                .setJavaPackage(JAVA_PACKAGE)
                                .build())
                        .addMessageType(DescriptorProtos.DescriptorProto.newBuilder()
                                .setName(MESSAGE_NAME)
                                .build()).build()
        ));
        stringJavaTypeNamesMap.forEach((s, t) -> System.out.println(s + " => " + t.describe()));
        assertThat(stringJavaTypeNamesMap.get(MESSAGE_NAME).boxed(),
                is(JAVA_PACKAGE + TypeUtils.PACKAGE_SEPERATOR + CamelCase(MESSAGE_NAME)));
    }
}