package com.boeckerman.jake.protobuf.nested;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.compiler.PluginProtos;
import junit.framework.TestCase;

import java.io.IOException;

public class RootModificationsTest extends TestCase {
    public void testEmpty() {
        new RootModifications(PluginProtos.CodeGeneratorRequest.getDefaultInstance()).generate();
    }

    public void testSimple() throws IOException {
        String FILE_NAME = "foo";
        RootModifications UNDER_TEST = new RootModifications(PluginProtos.CodeGeneratorRequest.newBuilder()
                .setParameter("RUN_EVERYWHERE")
                .addFileToGenerate(FILE_NAME)
                .addProtoFile(DescriptorProtos.FileDescriptorProto.newBuilder()
                        .setName(FILE_NAME)
                        .addMessageType(DescriptorProtos.DescriptorProto.newBuilder()
                                .setName("messageName")
                                .addField(DescriptorProtos.FieldDescriptorProto.newBuilder()
                                        .setName("example_field_asdf_primitive")
                                        .setType(DescriptorProtos.FieldDescriptorProto.Type.TYPE_BOOL)
                                        .setLabel(DescriptorProtos.FieldDescriptorProto.Label.LABEL_OPTIONAL)
                                        .build())
                                .build())
                        .build())
                .build());

        PluginProtos.CodeGeneratorResponse generated = UNDER_TEST.generate();
        com.google.protobuf.util.JsonFormat.printer().appendTo(generated, System.out);
    }
}