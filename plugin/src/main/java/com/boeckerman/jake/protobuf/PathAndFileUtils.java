package com.boeckerman.jake.protobuf;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.google.protobuf.DescriptorProtos.*;

public class PathAndFileUtils {
    public static final String JAVA_FILENAME_SUFFIX = ".java";
    public static final String OBLIGATORY_PATH_SEPARATOR = "/"; // protoc requires forward-slash, not backslash. Even on Windows.
    public static final String PACKAGE_SEPERATOR = ".";

    private static PathAndFileUtils instance = new PathAndFileUtils();

    public static PathAndFileUtils getInstance() {
        return instance;
    }

    public String insertionPointTypename(DescriptorProto descriptorProto,
                                         FileDescriptorProto fileDescriptorProto) {
        if (fileDescriptorProto.hasPackage()) {
            return fileDescriptorProto.getPackage() + PACKAGE_SEPERATOR + descriptorProto.getName();
        }
        return descriptorProto.getName();
    }

    public String fileToModify(FileDescriptorProto fileDescriptorProto,
                               DescriptorProto descriptorProto) {
        return fileToModify(fileDescriptorProto, descriptorProto,"");
    }
    public String fileToModify(FileDescriptorProto fileDescriptorProto,
                               DescriptorProto descriptorProto,
                               String filenameSuffix) {
        StringBuilder out = new StringBuilder();

        out.append(packageToPath(getPackage(fileDescriptorProto)));

        if (out.length() > 0) {
            out.append(OBLIGATORY_PATH_SEPARATOR);
        }

        out.append(getFileNameForClassDefinition(fileDescriptorProto, descriptorProto, filenameSuffix));

        out.append(JAVA_FILENAME_SUFFIX);
        return out.toString();
    }

    //filenameSuffix like "OrBuilder" or "_Mixin"
    private String getClassFileNameFor(FileDescriptorProto fileDescriptorProto, DescriptorProto descriptorProto) {
        return getFileNameForClassDefinition(fileDescriptorProto, descriptorProto, "");
    }

    //filenameSuffix like "OrBuilder" or "_Mixin"
    private String getFileNameForClassDefinition(FileDescriptorProto fileDescriptorProto, DescriptorProto descriptorProto, String filenameSuffix) {
        FileOptions options = fileDescriptorProto.getOptions();
        if (options.getJavaMultipleFiles()) {
            return descriptorProto.getName() + filenameSuffix;
        } else if (options.hasJavaOuterClassname()) {
            return options.getJavaOuterClassname();
        } else {
            return outerClassNameForFile(fileDescriptorProto);
        }
    }

    private String getPackage(FileDescriptorProto fileDescriptorProto) {
        FileOptions options = fileDescriptorProto.getOptions();
        return options.hasJavaPackage() ? options.getJavaPackage() : fileDescriptorProto.getPackage();
    }

    private static final Pattern TRAILING_PROTO_SUFFIX = Pattern.compile("\\.proto$");
    public static final String OUTER_CLASS_SUFFIX = "OuterClass";
    private String outerClassNameForFile(FileDescriptorProto fileDescriptorProto) {
        String guess = CamelCase(deleteMatchesOfPattern(TRAILING_PROTO_SUFFIX, fileDescriptorProto.getName()));

        // minor concern: This might be inefficient.
        // If our program is slow, we should count executions
        if(fileDescriptorProto.getMessageTypeList()
                .stream()
                .map(this::classNameForMessageDescriptor)
                .noneMatch(guess::equals)) {
            return guess;
        } else {
            return guess + OUTER_CLASS_SUFFIX;
        }
    }

    private static String deleteMatchesOfPattern(Pattern pattern, String string) {
        return pattern.matcher(string).replaceAll("");
    }

    private String classNameForMessageDescriptor(DescriptorProto descriptorProto) {
        return CamelCase(descriptorProto.getName());
    }

    private static final Pattern SINGLE_DOT = Pattern.compile("\\.");
    private static String packageToPath(String javaPackage) {
        return SINGLE_DOT.matcher(javaPackage).replaceAll(OBLIGATORY_PATH_SEPARATOR);
    }

    // Any run (+) of any kind ([]) of non-alphanumeric (non=^, alphanumeric=\p{Alnum}) characters
    private static final String NON_ALNUM_CHARACTERS = "[^\\p{Alnum}]+";
    // This regex matches exactly zero characters. The (?<=) looks for a Digit before this zero character spot.
    // The (?=) looks for an Alpha(betical) character after this zero-character point.
    private static final String ZERO_WIDTH_BETWEEN_DIGIT_AND_LETTER = "(?<=\\p{Digit})(?=\\p{Alpha})";

    /**
     * The matches of this regex are deleted, and then everything between them is capitalized and smushed together
     * We match and consume any non-alnum characters (see above), but also match the zero-width match between a digit and a letter
     */
    static final Pattern WORD_BREAK_PATTERN = Pattern.compile(NON_ALNUM_CHARACTERS + "|" + ZERO_WIDTH_BETWEEN_DIGIT_AND_LETTER);

    public static String CamelCase(String name_with_underscoresOrHyphens) {
        return WORD_BREAK_PATTERN.splitAsStream(name_with_underscoresOrHyphens)
                .map(StringUtils::capitalize)
                .collect(Collectors.joining());
    }
}
