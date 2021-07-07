package com.boeckerman.jake.protobuf;

import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.File;
import org.apache.commons.lang3.StringUtils;

public enum InsertionPoint {
    builder_implements,
    builder_scope,
    class_scope,
    interface_extends,
    message_implements,
    outer_class_scope {
        // The outer class scope does not take a typeName suffix
        // It's for the whole proto file!
        public String insertionPointFor(GeneratedResponseFileCoordinates fileIdentifier) {
            return this.name();
        }

        public boolean recognizes(File file) {
            return StringUtils.equals(this.name(), file.getInsertionPoint());
        }
    },
    enum_scope,

    // provided by this plugin
    custom_mixin_interface_scope;

    public static final String INSERTION_POINT_JOIN = ":";

    public boolean recognizes(File file) {
        return file.getInsertionPoint().startsWith(this.name() + INSERTION_POINT_JOIN);
    }

    public File.Builder fileBuilderFor(GeneratedResponseFileCoordinates fileIdentifier) {
        return File.newBuilder()
                .setName(fileIdentifier.fileToModify())
                .setInsertionPoint(insertionPointFor(fileIdentifier));
    }

    public String insertionPointFor(GeneratedResponseFileCoordinates fileIdentifier) {
        return this.name() + INSERTION_POINT_JOIN + fileIdentifier.insertionPointTypeName();
    }
}
