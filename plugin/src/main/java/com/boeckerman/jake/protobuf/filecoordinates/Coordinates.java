package com.boeckerman.jake.protobuf.filecoordinates;

import com.boeckerman.jake.protobuf.CodeGeneratorUtils;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.compiler.PluginProtos;

import java.util.regex.Pattern;

import static com.boeckerman.jake.protobuf.filecoordinates.InsertionPoint.INSERTION_POINT_JOIN;

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

    String modificationFileAndPath() {
        StringBuilder out = new StringBuilder(modificationJavaPackagePath());

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
            return outerClassFileBaseNameFor(fileDescriptorProto());
        }
    }

    static String PACKAGE_SEPERATOR = ".";

    String insertionPointTypeName() {
        String messageDescriptorTypename = descriptorProto().getName();
        DescriptorProtos.FileDescriptorProto fileDescriptorProto = fileDescriptorProto();

        if (fileDescriptorProto.hasPackage()) {
            return fileDescriptorProto.getPackage() + PACKAGE_SEPERATOR + messageDescriptorTypename;
        }
        return messageDescriptorTypename;
    }


    String insertionPointFor() {
        // outer class scope is not predicated on any message - it's shared for all messages in a .proto file
        if (insertionPoint == InsertionPoint.outer_class_scope) {
            return insertionPoint.name();
        }
        return insertionPoint.name() + INSERTION_POINT_JOIN + insertionPointTypeName();
    }

    String modificationJavaPackagePath() { // TODO: inline me!
        return CodeGeneratorUtils.packageToPath(javaPackage());
    }

    // see modificationFileAndPath
    StringBuilder javaFullClassNameBuilder() {
        StringBuilder out = new StringBuilder(javaPackage());

        if (out.length() > 0) {
            out.append(PACKAGE_SEPERATOR);
        }

        out.append(javaClassName());

        return out;
    }

    String javaClassName() {
        // TODO: THIS IS THE BUG SOURCE. Should use classNameForMessageDescriptor(descriptor())
        return insertionPoint.mangleJavaClassName(modificationFileBaseName());
    }

    String javaPackage() {
        DescriptorProtos.FileOptions options = fileDescriptorProto().getOptions();

        if (options.hasJavaPackage()) {
            return options.getJavaPackage();
        } else if (fileDescriptorProto().hasPackage()) {
            return fileDescriptorProto().getPackage();
        } else {
            return "";
        }
    }


    private static Pattern TRAILING_PROTO_SUFFIX = Pattern.compile("\\.proto$");
    private static String OUTER_CLASS_SUFFIX = "OuterClass";

    static String outerClassFileBaseNameFor(DescriptorProtos.FileDescriptorProto fileDescriptorProto) {
        String guess = CodeGeneratorUtils.CamelCase(CodeGeneratorUtils.deleteMatchesOfPattern(TRAILING_PROTO_SUFFIX, fileDescriptorProto.getName()));

        // minor concern: This might be inefficient.
        // If our program is slow, we should count executions
        if (fileDescriptorProto.getMessageTypeList()
                .stream()
                .map(Coordinates::classNameForMessageDescriptor)
                .noneMatch(guess::equals)) {
            return guess;
        } else {
            return guess + OUTER_CLASS_SUFFIX;
        }
    }

    static String classNameForMessageDescriptor(DescriptorProtos.DescriptorProto descriptorProto) {
        return CodeGeneratorUtils.CamelCase(descriptorProto.getName());
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
}
