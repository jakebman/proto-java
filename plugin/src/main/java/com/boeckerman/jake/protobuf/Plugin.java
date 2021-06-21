package com.boeckerman.jake.protobuf;

import org.apache.commons.lang3.text.WordUtils;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.protobuf.DescriptorProtos.*;
import static com.google.protobuf.compiler.PluginProtos.*;
import static com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.*;

public class Plugin {
    public static void main(String[] args) throws IOException {
        CodeGeneratorRequest request = CodeGeneratorRequest.parseFrom(System.in);
        CodeGenerator generator = new CodeGeneratorImpl(request);

        generator.generate().writeTo(System.out);
    }
}
