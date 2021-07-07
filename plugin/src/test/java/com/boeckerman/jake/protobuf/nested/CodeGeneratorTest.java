package com.boeckerman.jake.protobuf.nested;

import com.boeckerman.jake.protobuf.CodeGenerator;
import com.boeckerman.jake.protobuf.CodeGeneratorImpl;
import com.boeckerman.jake.protobuf.Context;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.compiler.PluginProtos;
import junit.framework.TestCase;

import java.io.IOException;

public class CodeGeneratorTest extends TestCase {
    CodeGenerator UNDER_TEST = new CodeGeneratorImpl();

    public void testEmpty() {
        UNDER_TEST.generate(PluginProtos.CodeGeneratorRequest.getDefaultInstance());
    }

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
        assertTrue("The file modified needs to be an OrBuilder",
                generated
                        .getFileList()
                        .stream()
                        .map(PluginProtos.CodeGeneratorResponse.File::getName)
                        .anyMatch(name -> name.endsWith("OrBuilder.java")));
    }
}