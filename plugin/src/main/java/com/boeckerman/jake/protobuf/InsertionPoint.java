package com.boeckerman.jake.protobuf;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.compiler.PluginProtos;
import org.apache.commons.lang3.StringUtils;

public class InsertionPoint {
    public enum InsertionPointPrefix {
        builder_implements,
        builder_scope,
        class_scope{
            // class scope does not take a class suffix
            private String insertionPointFor(String filename) {
                return this.name();
            }

            public boolean matchesInsertionPointStr(String s) {
                return StringUtils.equals(s, this.name());
            }
        },
        interface_extends,
        message_implements,
        outer_class_scope,
        enum_scope,

        // provided by this plugin
        custom_mixin_interface_scope;

        private String insertionPointFor(String insertionPointTypename) {
            return insertionPointPrefix() + insertionPointTypename;
        }

        private String insertionPointPrefix() {
            return this.name() + ":";
        }

        public PluginProtos.CodeGeneratorResponse.File.Builder fileBuilderFor(DescriptorProtos.FileDescriptorProto fileDescriptorProto, DescriptorProtos.DescriptorProto descriptorProto) {
            return fileBuilderFor_(fileDescriptorProto, descriptorProto)
                    .setInsertionPoint(insertionPointFor(CodeGeneratorUtils.insertionPointTypename(descriptorProto, fileDescriptorProto)));
        }

        public static  PluginProtos.CodeGeneratorResponse.File customMixinFile(DescriptorProtos.FileDescriptorProto fileDescriptorProto, DescriptorProtos.DescriptorProto descriptorProto) {
            return custom_mixin_interface_scope.fileBuilderFor(fileDescriptorProto, descriptorProto)
                    .clearInsertionPoint() //
                    .build();
        }

        private static PluginProtos.CodeGeneratorResponse.File.Builder fileBuilderFor_(DescriptorProtos.FileDescriptorProto fileDescriptorProto, DescriptorProtos.DescriptorProto descriptorProto) {
            return PluginProtos.CodeGeneratorResponse.File.newBuilder()
                    .setName(CodeGeneratorUtils.fileToModify(fileDescriptorProto, descriptorProto));
        }

        public boolean matchesInsertionPointStr(String s) {
            return StringUtils.startsWith(s, this.insertionPointPrefix());
        }
    }
}
