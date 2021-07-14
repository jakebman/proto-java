package com.charter.nns.protobuf;

import com.google.protobuf.compiler.PluginProtos.CodeGeneratorRequest;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse;

public interface CodeGenerator {
    CodeGeneratorResponse generate(CodeGeneratorRequest request);
}
