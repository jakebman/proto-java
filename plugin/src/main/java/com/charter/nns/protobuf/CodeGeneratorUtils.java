package com.charter.nns.protobuf;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.TextFormat;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorRequest;
import com.google.protobuf.util.JsonFormat;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

    public static final JsonFormat.Printer JSON_PRINTER = JsonFormat.printer().includingDefaultValueFields().omittingInsignificantWhitespace();

    public static String debugPeek(Message message) {
        try {
            return JSON_PRINTER.print(message);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
            return TextFormat.printer().shortDebugString(message);
        }
    }

    static CodeGeneratorRequest filter(CodeGeneratorRequest request) {
        return request.toBuilder()
                .clearProtoFile()
                .addAllProtoFile(request.getProtoFileList()
                        .stream()
                        .map(CodeGeneratorUtils::filter)
                        .collect(Collectors.toList()))
                .build();
    }

    private static FileDescriptorProto filter(FileDescriptorProto protoFile) {
        return protoFile.toBuilder()
                .setSourceCodeInfo(DescriptorProtos.SourceCodeInfo.newBuilder()
                        .addLocation(DescriptorProtos.SourceCodeInfo.Location.newBuilder()
                                .addLeadingDetachedComments("SourceCodeInfo elided from debug info")
                                .build())
                        .build())
                .build();
    }
}
