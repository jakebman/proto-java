package com.boeckerman.jake.protobuf;

import org.apache.commons.lang3.StringUtils;

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
            out.append(classNameForFile(fileDescriptorProto));
        }
        out.append(".java");
        return out.toString();
    }

    private static final Pattern TRAILING_PROTO_SUFFIX = Pattern.compile("\\.proto$");
    private static String classNameForFile(FileDescriptorProto fileDescriptorProto) {
        String guess = CamelCase(deleteMatchesOfPattern(TRAILING_PROTO_SUFFIX, fileDescriptorProto.getName())); // TODO: compile regex

        // minor concern: This might be inefficient.
        // If our program is slow, we should count executions
        if(fileDescriptorProto.getMessageTypeList()
                .stream()
                .map(CodeGeneratorUtils::classNameForMessageDescriptor)
                .noneMatch(guess::equals)) {
            return guess;
        } else {
            return guess + "OuterClass";
        }
    }

    private static String deleteMatchesOfPattern(Pattern pattern, String string) {
        return pattern.matcher(string).replaceAll("");
    }

    private static String classNameForMessageDescriptor(DescriptorProto descriptorProto) {
        return CamelCase(descriptorProto.getName());
    }

    private static String packageToPath(String javaPackage) {
        return SINGLE_DOT.matcher(javaPackage).replaceAll("/");
    }

    // \b is a "word barrier" - zero-width match between a letter and a non-letter
    // There might be some hilarious interaction between what \b thinks is a letter and what
    // IsAlphabetic thinks is a letter. Regex on unicode is fun.
    static final Pattern CAPITALIZABLE_CHARACTER = Pattern.compile("\\b(\\p{IsAlphabetic})");
    static final Pattern NON_ALPHABETICAL_CHARACTER = Pattern.compile("[^\\p{IsAlphabetic}]+");

    static String CamelCase(String name_with_underscoresOrHyphens) {
        String nameWithCorrectCaps = CAPITALIZABLE_CHARACTER.matcher(name_with_underscoresOrHyphens)
                .replaceAll(matchResult -> StringUtils.capitalize(matchResult.group()));
        String nameWithoutNonCharacters = deleteMatchesOfPattern(NON_ALPHABETICAL_CHARACTER, nameWithCorrectCaps);
        return nameWithoutNonCharacters;
    }
}
