package com.boeckerman.jake.protobuf;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.compiler.PluginProtos;

public enum InsertionPoint {
    builder_implements,
    builder_scope,
    class_scope{
        // class scope does not take a class suffix
        private String insertionPointFor(String filename) {
            return this.name();
        }
    },
    interface_extends,
    message_implements,
    outer_class_scope,
    enum_scope,

    // provided by this plugin
    custom_mixin_file;

    private String insertionPointFor(String insertionPointTypename) {
        return this.name() + ":" + insertionPointTypename;
    }

    public PluginProtos.CodeGeneratorResponse.File.Builder fileBuilderFor(DescriptorProtos.FileDescriptorProto fileDescriptorProto, DescriptorProtos.DescriptorProto descriptorProto) {
        return fileBuilderFor_(fileDescriptorProto, descriptorProto)
                .setInsertionPoint(insertionPointFor(CodeGeneratorUtils.insertionPointTypename(descriptorProto, fileDescriptorProto)));
    }

    public static  PluginProtos.CodeGeneratorResponse.File customMixinFile(DescriptorProtos.FileDescriptorProto fileDescriptorProto, DescriptorProtos.DescriptorProto descriptorProto) {
        return custom_mixin_file.fileBuilderFor(fileDescriptorProto, descriptorProto)
                .clearInsertionPoint() //
                .build();
    }

    private static PluginProtos.CodeGeneratorResponse.File.Builder fileBuilderFor_(DescriptorProtos.FileDescriptorProto fileDescriptorProto, DescriptorProtos.DescriptorProto descriptorProto) {
        return PluginProtos.CodeGeneratorResponse.File.newBuilder()
                .setName(CodeGeneratorUtils.fileToModify(fileDescriptorProto, descriptorProto));
    }
}
