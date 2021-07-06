package com.boeckerman.jake.protobuf;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.google.protobuf.DescriptorProtos.*;

public class CodeGeneratorUtils {
    public static final String JAVA_FILENAME_SUFFIX = ".java";
    public static final String OBLIGATORY_PATH_SEPARATOR = "/"; // protoc requires forward-slash, not backslash. Even on Windows.
    public static final String PACKAGE_SEPERATOR = ".";

    public static String insertionPointTypename(DescriptorProto descriptorProto,
                                                FileDescriptorProto fileDescriptorProto) {
        if (fileDescriptorProto.hasPackage()) {
            return fileDescriptorProto.getPackage() + PACKAGE_SEPERATOR + descriptorProto.getName();
        }
        return descriptorProto.getName();
    }

    public static String fileToModify(FileDescriptorProto fileDescriptorProto,
                                      DescriptorProto descriptorProto) {
        FileOptions options = fileDescriptorProto.getOptions();
        StringBuilder out = new StringBuilder();

        if (options.hasJavaPackage()) {
            out.append(packageToPath(options.getJavaPackage()));
        } else if (fileDescriptorProto.hasPackage()) {
            out.append(packageToPath(fileDescriptorProto.getPackage()));
        }
        if (out.length() > 0) {
            out.append(OBLIGATORY_PATH_SEPARATOR);
        }

        if (options.getJavaMultipleFiles()) {
            out.append(descriptorProto.getName());
        } else if (options.hasJavaOuterClassname()) {
            out.append(options.getJavaOuterClassname());
        } else {
            out.append(outerClassNameForFile(fileDescriptorProto));
        }
        out.append(JAVA_FILENAME_SUFFIX);
        return out.toString();
    }

    private static final Pattern TRAILING_PROTO_SUFFIX = Pattern.compile("\\.proto$");
    public static final String OUTER_CLASS_SUFFIX = "OuterClass";

    private static String outerClassNameForFile(FileDescriptorProto fileDescriptorProto) {
        String guess = CamelCase(deleteMatchesOfPattern(TRAILING_PROTO_SUFFIX, fileDescriptorProto.getName()));

        // minor concern: This might be inefficient.
        // If our program is slow, we should count executions
        if (fileDescriptorProto.getMessageTypeList()
                .stream()
                .map(CodeGeneratorUtils::classNameForMessageDescriptor)
                .noneMatch(guess::equals)) {
            return guess;
        } else {
            return guess + OUTER_CLASS_SUFFIX;
        }
    }

    private static String deleteMatchesOfPattern(Pattern pattern, String string) {
        return pattern.matcher(string).replaceAll("");
    }

    private static String classNameForMessageDescriptor(DescriptorProto descriptorProto) {
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

    public static Function<String, FileDescriptorProto> fileNameToProtoFileDescriptorLookup(List<FileDescriptorProto> protoFileList) {
        Map<String, FileDescriptorProto> lookup = protoFileList.stream()
                .collect(Collectors.toMap(FileDescriptorProto::getName, Function.identity()));
        return lookup::get;
    }

    public static boolean isPrimitive(FieldDescriptorProto.Type type) {
        return switch (type) {
            case TYPE_DOUBLE, TYPE_FLOAT, // floating-point
                    TYPE_FIXED64, TYPE_SFIXED64, // fixed-point, wide
                    TYPE_INT64, TYPE_UINT64, TYPE_SINT64, // ints, wide
                    TYPE_FIXED32, TYPE_SFIXED32,  // fixed-point
                    TYPE_INT32, TYPE_UINT32, TYPE_SINT32, // int
                    TYPE_BOOL -> true;
            case TYPE_STRING, TYPE_GROUP, TYPE_MESSAGE, TYPE_BYTES, TYPE_ENUM -> false;
            // Protobuf is allowed to add new Enums to this class. In that case, java throws this error, but without a message
            // https://docs.oracle.com/javase/specs/jls/se16/html/jls-15.html#jls-15.28.2
            default -> throw new IncompatibleClassChangeError( type + " was not a valid value at compile time");
        };
    }
}
