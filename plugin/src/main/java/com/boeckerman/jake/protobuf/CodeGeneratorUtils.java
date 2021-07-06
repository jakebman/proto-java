package com.boeckerman.jake.protobuf;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import static com.google.protobuf.DescriptorProtos.FileDescriptorProto;

public class CodeGeneratorUtils {
    public static final String OBLIGATORY_PATH_SEPARATOR = "/"; // protoc requires forward-slash, not backslash. Even on Windows.

    public static String deleteMatchesOfPattern(Pattern pattern, String string) {
        return pattern.matcher(string).replaceAll("");
    }

    private static final Pattern SINGLE_DOT = Pattern.compile("\\.");

    public static String packageToPath(String javaPackage) {
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
            default -> throw new IncompatibleClassChangeError(type + " was not a valid value at compile time");
        };
    }
}
