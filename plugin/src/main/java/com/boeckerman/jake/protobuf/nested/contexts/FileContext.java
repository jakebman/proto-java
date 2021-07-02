package com.boeckerman.jake.protobuf.nested.contexts;

import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorRequest;

public interface FileContext extends RootContext {
    FileDescriptorProto getFileDescriptorProto();

    RootContext delegate();

    @Override
    default CodeGeneratorRequest getCodeGeneratorRequest() {
        return delegate().getCodeGeneratorRequest();
    }

    @Override
    default InvocationParameters getInvocationParameters() {
        return delegate().getInvocationParameters();
    }
}
