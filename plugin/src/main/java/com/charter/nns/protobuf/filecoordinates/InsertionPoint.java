package com.charter.nns.protobuf.filecoordinates;

import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.File;
import org.apache.commons.lang3.StringUtils;

public enum InsertionPoint {
    builder_implements(".Builder", ""), // inner class
    builder_scope(".Builder", ""), // inner class
    class_scope,
    interface_extends("OrBuilder"),
    message_implements,
    outer_class_scope {
        boolean recognizes(File file) {
            return StringUtils.equals(this.name(), file.getInsertionPoint());
        }
    },
    enum_scope,

    // provided by this plugin
    custom_mixin_interface_scope(CustomMixinFile.MIXIN_SUFFIX);

    // suffix added to a message's name to determine the class this insertion point is about
    // (This should only really matter for custom_mixin_interface_scope, which wants to talk about
    // a Person_Mixin
    private final String classNameSuffix;
    // suffix added to a message's name to determine the bare name of the .java file which should be
    // modified to be related to the current insertion point. (The java_outer_class_name logic is
    // already handled by Coordinates)
    private final String fileNameSuffix_for_java_multiple_files;

    InsertionPoint() {
        this("");
    }

    InsertionPoint(String allNameSuffix) {
        this(allNameSuffix, allNameSuffix);
    }

    InsertionPoint(String classNameSuffix, String fileNameSuffix_for_java_multiple_files) {
        this.classNameSuffix = classNameSuffix;
        this.fileNameSuffix_for_java_multiple_files = fileNameSuffix_for_java_multiple_files;
    }

    static final String INSERTION_POINT_JOIN = ":";

    //package-private
    boolean recognizes(File file) {
        return file.getInsertionPoint().startsWith(this.name() + INSERTION_POINT_JOIN);
    }

    String mangleJavaClassName(String messageName) {
        return messageName + classNameSuffix;
    }

    String mangleBaseFileNameFor_java_multiple_files(String messageName) {
        return messageName + fileNameSuffix_for_java_multiple_files;
    }
}
