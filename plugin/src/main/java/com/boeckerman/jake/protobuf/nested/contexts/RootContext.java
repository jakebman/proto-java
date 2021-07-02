package com.boeckerman.jake.protobuf.nested.contexts;

import com.google.protobuf.compiler.PluginProtos.CodeGeneratorRequest;

public interface RootContext {
    CodeGeneratorRequest getCodeGeneratorRequest();
}
