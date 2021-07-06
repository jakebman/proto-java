package com.boeckerman.jake.protobuf;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.compiler.PluginProtos;
import org.apache.commons.lang3.StringUtils;

public class InsertionPoint {

    public static final String OR_BUILDER = "OrBuilder";

    public static PluginProtos.CodeGeneratorResponse.File customMixinFile(DescriptorProtos.FileDescriptorProto fileDescriptorProto, DescriptorProtos.DescriptorProto descriptorProto) {
        return InsertionPointPrefix.custom_mixin_interface_scope.fileBuilderFor(fileDescriptorProto, descriptorProto)
                .clearInsertionPoint() // so we write the whole file
                .setContent("...")
                .build();
    }

    public enum InsertionPointPrefix {
        builder_implements,
        builder_scope,
        class_scope {
            // class scope does not take a class suffix
            private String insertionPointFor(String filename) {
                return this.name();
            }

            public boolean matchesInsertionPointStr(String s) {
                return StringUtils.equals(s, this.name());
            }
        },
        interface_extends(OR_BUILDER),
        message_implements,
        outer_class_scope, // TODO
        enum_scope,

        // provided by this plugin
        custom_mixin_interface_scope("_Mixin") {

            public String className() {
                return null;
            }
        };

        final PathAndFileUtils pathAndFileUtils;
        private final String classFileSuffix;

        InsertionPointPrefix() {
            this("");
        }

        InsertionPointPrefix(String classFileSuffix) {
            this.pathAndFileUtils = PathAndFileUtils.getInstance();
            this.classFileSuffix = classFileSuffix;
        }

        private String insertionPointFor(String insertionPointTypename) {
            return insertionPointPrefix() + insertionPointTypename;
        }

        private String insertionPointPrefix() {
            return this.name() + ":";
        }

        public PluginProtos.CodeGeneratorResponse.File.Builder fileBuilderFor(DescriptorProtos.FileDescriptorProto fileDescriptorProto, DescriptorProtos.DescriptorProto descriptorProto) {
            return PluginProtos.CodeGeneratorResponse.File.newBuilder()
                    .setName(fileToModify(fileDescriptorProto, descriptorProto))
                    .setInsertionPoint(insertionPointFor(pathAndFileUtils.insertionPointTypename(descriptorProto, fileDescriptorProto)));
        }

        private String fileToModify(DescriptorProtos.FileDescriptorProto fileDescriptorProto, DescriptorProtos.DescriptorProto descriptorProto) {
            return pathAndFileUtils.fileToModify(fileDescriptorProto, descriptorProto, classFileSuffix);
        }

        public boolean matchesInsertionPointStr(String s) {
            return StringUtils.startsWith(s, this.insertionPointPrefix());
        }
    }
}
