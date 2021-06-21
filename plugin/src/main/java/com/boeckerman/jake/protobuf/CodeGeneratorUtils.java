package com.boeckerman.jake.protobuf;

import org.apache.commons.lang3.text.WordUtils;

import java.util.regex.Pattern;

import static com.google.protobuf.DescriptorProtos.*;

public class CodeGeneratorUtils {
    private static final Pattern SINGLE_DOT = Pattern.compile("\\.");

    static String insertionPointTypename(DescriptorProto descriptorProto,
                                         FileDescriptorProto fileDescriptorProto) {
        if (fileDescriptorProto.hasPackage()) {
            return fileDescriptorProto.getPackage() + "." + descriptorProto.getName();
        }
        return descriptorProto.getName();
    }

    static String fileToModify(FileDescriptorProto fileDescriptorProto,
                               DescriptorProto descriptorProto) {
        FileOptions options = fileDescriptorProto.getOptions();
        StringBuilder out = new StringBuilder();

        if (options.hasJavaPackage()) {
            out.append(packageToPath(options.getJavaPackage()));
        } else if (fileDescriptorProto.hasPackage()) {
            out.append(packageToPath(fileDescriptorProto.getPackage()));
        }
        if (out.length() > 0) {
            out.append("/");
        }

        if (options.getJavaMultipleFiles()) {
            out.append(descriptorProto.getName());
        } else if (options.hasJavaOuterClassname()) {
            out.append(options.getJavaOuterClassname());
        } else {
            out.append(CamelCase(descriptorProto.getName()))
                    .append("OuterClass");
        }
        out.append(".java");
        return out.toString();
    }

    private static String packageToPath(String javaPackage) {
        return SINGLE_DOT.matcher(javaPackage).replaceAll("/");
    }

    static String CamelCase(String name_with_underscores) {
        return WordUtils.capitalizeFully(name_with_underscores, '_');
    }
}
