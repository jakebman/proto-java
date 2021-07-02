package com.boeckerman.jake.protobuf.nested.contexts;

import com.google.protobuf.compiler.PluginProtos.CodeGeneratorRequest;

public interface RootContext {
    CodeGeneratorRequest getCodeGeneratorRequest();

    InvocationParameters getInvocationParameters();

    class InvocationParameters {
        public boolean run_everywhere;

        public InvocationParameters(String params) {
            run_everywhere = params.contains("RUN_EVERYWHERE");
        }
    }
}
