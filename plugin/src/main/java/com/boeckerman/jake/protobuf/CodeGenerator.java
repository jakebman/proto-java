package com.boeckerman.jake.protobuf;

import com.google.protobuf.ByteString;
import com.google.protobuf.compiler.PluginProtos;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorRequest;

public interface CodeGenerator {
    PluginProtos.CodeGeneratorResponse generate(CodeGeneratorRequest request);
}
