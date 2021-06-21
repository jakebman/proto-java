package com.boeckerman.jake.protobuf;

import com.google.protobuf.ByteString;
import com.google.protobuf.compiler.PluginProtos;

public interface CodeGenerator {
    PluginProtos.CodeGeneratorResponse generate();
}
