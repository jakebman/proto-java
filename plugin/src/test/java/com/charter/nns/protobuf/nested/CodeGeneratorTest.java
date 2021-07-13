package com.charter.nns.protobuf.nested;

import com.charter.nns.protobuf.CodeGenerator;
import com.charter.nns.protobuf.CodeGeneratorImpl;
import com.charter.nns.protobuf.Context;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.compiler.PluginProtos;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class CodeGeneratorTest {
    CodeGenerator UNDER_TEST = new CodeGeneratorImpl();

    @Test
    public void testEmpty() {
        UNDER_TEST.generate(PluginProtos.CodeGeneratorRequest.getDefaultInstance());
    }

    @Test
    public void testSimple() throws IOException {
        String FILE_NAME = "foo";

        PluginProtos.CodeGeneratorResponse generated = UNDER_TEST.generate(PluginProtos.CodeGeneratorRequest.newBuilder()
                .setParameter(Context.ENABLE_EVERYWHERE)
                .addFileToGenerate(FILE_NAME)
                .addProtoFile(DescriptorProtos.FileDescriptorProto.newBuilder()
                        .setName(FILE_NAME)
                        .setOptions(DescriptorProtos.FileOptions.newBuilder()
                                .setJavaMultipleFiles(true)
                                .build())
                        .addMessageType(DescriptorProtos.DescriptorProto.newBuilder()
                                .setName("messageName")
                                .addField(DescriptorProtos.FieldDescriptorProto.newBuilder()
                                        .setName("example_field_asdf_primitive") // trigger the NullableOptions processing, which uses custom_mixin_interface_scope
                                        .setType(DescriptorProtos.FieldDescriptorProto.Type.TYPE_BOOL)
                                        .setLabel(DescriptorProtos.FieldDescriptorProto.Label.LABEL_OPTIONAL)
                                        .build())
                                .build())
                        .build())
                .build());
        com.google.protobuf.util.JsonFormat.printer().appendTo(generated, System.out);
        Assert.assertTrue("The file modified needs to be an OrBuilder",
                generated
                        .getFileList()
                        .stream()
                        .map(PluginProtos.CodeGeneratorResponse.File::getName)
                        .anyMatch(name -> name.endsWith("OrBuilder.java")));
    }
}