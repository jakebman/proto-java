package com.charter.nns.protobuf.filecoordinates;

import com.charter.nns.protobuf.CodeGeneratorUtils;
import com.charter.nns.protobuf.TypeUtils;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.compiler.PluginProtos;

import javax.annotation.Nullable;

import static com.charter.nns.protobuf.filecoordinates.InsertionPoint.INSERTION_POINT_JOIN;

// package-private
record Coordinates(GeneratedResponseFileCoordinates fileIdentifier,
                   InsertionPoint insertionPoint) implements GeneratedResponseFileCoordinates {

    //The most public entrance to this class
    PluginProtos.CodeGeneratorResponse.File.Builder fileBuilder() {
        return PluginProtos.CodeGeneratorResponse.File.newBuilder()
                .setName(modificationFileAndPath())
                .setInsertionPoint(insertionPointFor());
    }

    static String JAVA_FILENAME_SUFFIX = ".java";

    // see javaFullClassNameBuilder
    String modificationFileAndPath() {
        StringBuilder out = new StringBuilder(CodeGeneratorUtils.packageToPath(javaPackage()));

        if (out.length() > 0) {
            out.append(CodeGeneratorUtils.OBLIGATORY_PATH_SEPARATOR);
        }

        out.append(modificationFileBaseName());
        out.append(JAVA_FILENAME_SUFFIX);
        return out.toString();
    }

    String modificationFileBaseName() {
        DescriptorProtos.FileOptions options = fileDescriptorProto().getOptions();

        if (options.getJavaMultipleFiles()) {
            return insertionPoint.mangleBaseFileNameFor_java_multiple_files(descriptorProto().getName());
        } else if (options.hasJavaOuterClassname()) {
            return options.getJavaOuterClassname();
        } else {
            return TypeUtils.javaOuterClassBaseName(fileDescriptorProto());
        }
    }

    String insertionPointFor() {
        // outer class scope is not predicated on any message - it's shared for all messages in a .proto file
        if (insertionPoint == InsertionPoint.outer_class_scope) {
            return insertionPoint.name();
        }
        return insertionPoint.name() + INSERTION_POINT_JOIN + TypeUtils.protoFullTypeName(this);
    }

    // Warning: javaPackage + javaClass != javaFullClassName (nested classes)
    String javaClassName() {
        //This mangling is important to address OrBuilder and _Mixin interfaces
        return insertionPoint.mangleJavaClassName(TypeUtils.javaClassName(this));
    }

    // Warning: javaPackage + javaClass != javaFullClassName (nested classes)
    String javaPackage() {
        return TypeUtils.javaPackage(this);
    }

    // with-ers
    Coordinates withInsertionPoint(InsertionPoint insertionPoint) {
        // nb: the parameter insertion point is used here, not the field with the same name
        return new Coordinates(fileIdentifier, insertionPoint);
    }

    // delegation, so that Coordinates feels-like-a GeneratedResponseFileCoordinates
    public DescriptorProtos.FileDescriptorProto fileDescriptorProto() {
        return fileIdentifier().fileDescriptorProto();
    }

    public DescriptorProtos.DescriptorProto descriptorProto() {
        return fileIdentifier().descriptorProto();
    }

    @Nullable
    @Override
    public GeneratedResponseFileCoordinates parent() {
        return fileIdentifier().parent();
    }
}
