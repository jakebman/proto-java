package com.boeckerman.jake.protobuf.nested;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.compiler.PluginProtos;

import java.util.stream.Stream;

class FileDescriptorModifications implements NestedStreamingIterable<PluginProtos.CodeGeneratorResponse.File> {
    final RootModifications parent;
    final DescriptorProtos.FileDescriptorProto fileDescriptorProto;

    public FileDescriptorModifications(RootModifications parent, DescriptorProtos.FileDescriptorProto fileDescriptorProto) {
        this.parent = parent;
        this.fileDescriptorProto = fileDescriptorProto;
    }

    @Override
    public Stream<NestedStreamingIterable<PluginProtos.CodeGeneratorResponse.File>> children() {
        return fileDescriptorProto
                .getMessageTypeList()
                .stream()
                .map(this::generateChild);
    }

    private MessageDescriptorModifications generateChild(DescriptorProtos.DescriptorProto messageDescriptorProto) {
        return new MessageDescriptorModifications(this, messageDescriptorProto);
    }

}
