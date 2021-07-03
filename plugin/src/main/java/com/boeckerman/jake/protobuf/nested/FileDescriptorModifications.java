package com.boeckerman.jake.protobuf.nested;

import com.boeckerman.jake.protobuf.nested.contexts.FileContext;
import com.boeckerman.jake.protobuf.nested.contexts.RootContext;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.File;

import java.util.stream.Stream;

class FileDescriptorModifications implements NestedStreamingIterable<File>, FileContext {
    final RootModifications parent;
    final DescriptorProtos.FileDescriptorProto fileDescriptorProto;

    public FileDescriptorModifications(RootModifications parent, DescriptorProtos.FileDescriptorProto fileDescriptorProto) {
        this.parent = parent;
        this.fileDescriptorProto = fileDescriptorProto;
    }

    @Override
    public Stream<NestedStreamingIterable<File>> children() {
        return fileDescriptorProto
                .getMessageTypeList()
                .stream()
                .map(this::generateChild);
    }

    private MessageDescriptorModifications generateChild(DescriptorProtos.DescriptorProto messageDescriptorProto) {
        return new MessageDescriptorModifications(this, messageDescriptorProto);
    }

    // Context-passing code to help FieldDescriptorModifications read all necessary context
    @Override
    public DescriptorProtos.FileDescriptorProto getFileDescriptorProto() {
        return fileDescriptorProto;
    }

    @Override
    public RootContext delegate() {
        return parent;
    }
}
