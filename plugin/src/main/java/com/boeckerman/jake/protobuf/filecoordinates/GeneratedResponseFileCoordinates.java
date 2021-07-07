package com.boeckerman.jake.protobuf.filecoordinates;

import com.boeckerman.jake.protobuf.CodeGeneratorUtils;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.File;

import java.util.regex.Pattern;

// so we can get the file builder for any sufficiently-deep context
public interface GeneratedResponseFileCoordinates {
    // the two record fields that insertion points need to uniquely identify a file edit point
    FileDescriptorProto fileDescriptorProto();

    DescriptorProto descriptorProto();

    default File.Builder fileBuilderFor(InsertionPoint insertionPoint) {
        return insertionPoint.fileBuilderFor(this);
    }

    String PACKAGE_SEPERATOR = ".";
    default String insertionPointTypeName() {
        String messageDescriptorTypename = descriptorProto().getName();
        FileDescriptorProto fileDescriptorProto = fileDescriptorProto();

        if (fileDescriptorProto.hasPackage()) {
            return fileDescriptorProto.getPackage() + PACKAGE_SEPERATOR + messageDescriptorTypename;
        }
        return messageDescriptorTypename;
    }

    String JAVA_FILENAME_SUFFIX = ".java";
    default String modificationFileAndPath() {
        StringBuilder out = new StringBuilder(modificationJavaPackagePath());

        if (out.length() > 0) {
            out.append(CodeGeneratorUtils.OBLIGATORY_PATH_SEPARATOR);
        }

        out.append(modificationFileBaseName());
        out.append(JAVA_FILENAME_SUFFIX);
        return out.toString();
    }

    default String modificationFileBaseName() {
        DescriptorProtos.FileOptions options = fileDescriptorProto().getOptions();

        if (options.getJavaMultipleFiles()) {
            return descriptorProto().getName();
        } else if (options.hasJavaOuterClassname()) {
            return options.getJavaOuterClassname();
        } else {
            return outerClassFileBaseNameFor(fileDescriptorProto());
        }
    }

    default String modificationJavaPackagePath() {
        return CodeGeneratorUtils.packageToPath(javaPackage());
    }

    default String javaPackage() {
        FileDescriptorProto fileDescriptorProto = fileDescriptorProto();
        DescriptorProtos.FileOptions options = fileDescriptorProto().getOptions();

        if (options.hasJavaPackage()) {
            return options.getJavaPackage();
        } else if (fileDescriptorProto.hasPackage()) {
            return fileDescriptorProto.getPackage();
        } else {
            return "";
        }
    }


    Pattern TRAILING_PROTO_SUFFIX = Pattern.compile("\\.proto$");
    String OUTER_CLASS_SUFFIX = "OuterClass";

    static String outerClassFileBaseNameFor(FileDescriptorProto fileDescriptorProto) {
        String guess = CodeGeneratorUtils.CamelCase(CodeGeneratorUtils.deleteMatchesOfPattern(TRAILING_PROTO_SUFFIX, fileDescriptorProto.getName()));

        // minor concern: This might be inefficient.
        // If our program is slow, we should count executions
        if (fileDescriptorProto.getMessageTypeList()
                .stream()
                .map(GeneratedResponseFileCoordinates::classNameForMessageDescriptor)
                .noneMatch(guess::equals)) {
            return guess;
        } else {
            return guess + OUTER_CLASS_SUFFIX;
        }
    }

    static String classNameForMessageDescriptor(DescriptorProto descriptorProto) {
        return CodeGeneratorUtils.CamelCase(descriptorProto.getName());
    }
}
