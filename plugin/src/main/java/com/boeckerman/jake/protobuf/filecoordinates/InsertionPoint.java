package com.boeckerman.jake.protobuf.filecoordinates;

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
        String insertionPointFor(GeneratedResponseFileCoordinates fileIdentifier) {
            return this.name();
        }

        boolean recognizes(File file) {
            return StringUtils.equals(this.name(), file.getInsertionPoint());
        }
    },
    enum_scope,

    // provided by this plugin
    custom_mixin_interface_scope;

    static final String INSERTION_POINT_JOIN = ":";

    //package-private
    boolean recognizes(File file) {
        return file.getInsertionPoint().startsWith(this.name() + INSERTION_POINT_JOIN);
    }

    //package-private
    File.Builder fileBuilderFor(GeneratedResponseFileCoordinates fileIdentifier) {
        return File.newBuilder()
                .setName(fileIdentifier.modificationFileAndPath())
                .setInsertionPoint(insertionPointFor(fileIdentifier));
    }

    //package-private
    String insertionPointFor(GeneratedResponseFileCoordinates fileIdentifier) {
        return this.name() + INSERTION_POINT_JOIN + fileIdentifier.insertionPointTypeName();
    }
}
