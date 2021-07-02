package com.boeckerman.jake.protobuf.nested.contexts;

import com.boeckerman.jake.protobuf.Extensions.JavaExtensionOptions;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;

public interface MessageContext extends FileContext {
    DescriptorProto getMessageDescriptorProto();

    JavaExtensionOptions getMessageExtensions();

    @Override
    default FileDescriptorProto getFileDescriptorProto() {
        return delegate().getFileDescriptorProto();
    }

    @Override
    FileContext delegate();
}
