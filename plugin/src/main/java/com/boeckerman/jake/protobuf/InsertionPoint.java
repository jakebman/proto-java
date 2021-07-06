package com.boeckerman.jake.protobuf;

import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.File;

public enum InsertionPoint {
    builder_implements,
    builder_scope,
    class_scope {
        // class scope does not take a class suffix
        public String insertionPointFor(String filename) {
            return this.name();
        }
    },
    interface_extends,
    message_implements,
    outer_class_scope,
    enum_scope,

    // provided by this plugin
    custom_mixin_file; // would officially be more like "interface_scope"

    private static File.Builder fileBuilderFor_(GeneratedResponseFileCoordinates fileIdentifier) {
        return File.newBuilder()
                .setName(CodeGeneratorUtils.fileToModify(fileIdentifier));
    }

    public File.Builder fileBuilderFor(GeneratedResponseFileCoordinates fileIdentifier) {
        return fileBuilderFor_(fileIdentifier)
                .setInsertionPoint(insertionPointFor(CodeGeneratorUtils.insertionPointTypename(fileIdentifier)));
    }


    public String insertionPointFor(GeneratedResponseFileCoordinates fileIdentifier) {
        return insertionPointFor(CodeGeneratorUtils.insertionPointTypename(fileIdentifier));
    }

    public String insertionPointFor(String insertionPointTypename) {
        return this.name() + ":" + insertionPointTypename;
    }
}
