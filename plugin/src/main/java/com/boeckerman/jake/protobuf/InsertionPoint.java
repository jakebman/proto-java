package com.boeckerman.jake.protobuf;

import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.File;

public enum InsertionPoint {
    builder_implements,
    builder_scope,
    class_scope {
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
    custom_mixin_file; // would officially be more like "interface_scope"

    public static File customMixinFile(FileDescriptorProto fileDescriptorProto, DescriptorProto descriptorProto) {
        return custom_mixin_file.fileBuilderFor(fileDescriptorProto, descriptorProto)
                .clearInsertionPoint() //
                .build();
    }

    private static File.Builder fileBuilderFor_(FileDescriptorProto fileDescriptorProto, DescriptorProto descriptorProto) {
        return File.newBuilder()
                .setName(CodeGeneratorUtils.fileToModify(fileDescriptorProto, descriptorProto));
    }

    public File.Builder fileBuilderFor(Context.GeneratedResponseFileCoordinates fileIdentifier) {
        return fileBuilderFor(fileIdentifier.fileDescriptorProto(), fileIdentifier.descriptorProto());
    }

    private File.Builder fileBuilderFor(FileDescriptorProto fileDescriptorProto, DescriptorProto descriptorProto) {
        return fileBuilderFor_(fileDescriptorProto, descriptorProto)
                .setInsertionPoint(insertionPointFor(CodeGeneratorUtils.insertionPointTypename(descriptorProto, fileDescriptorProto)));
    }

    private String insertionPointFor(String insertionPointTypename) {
        return this.name() + ":" + insertionPointTypename;
    }
}
