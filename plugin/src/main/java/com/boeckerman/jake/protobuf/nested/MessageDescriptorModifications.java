package com.boeckerman.jake.protobuf.nested;

import com.boeckerman.jake.protobuf.Extensions;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.compiler.PluginProtos;

import java.util.Optional;
import java.util.stream.Stream;

class MessageDescriptorModifications implements NestedStreamingIterable<PluginProtos.CodeGeneratorResponse.File> {
    final FileDescriptorModifications parent;
    final DescriptorProtos.DescriptorProto messageDescriptorProto;
    final Extensions.JavaExtensionOptions messageExtensions;


    public MessageDescriptorModifications(FileDescriptorModifications parent, DescriptorProtos.DescriptorProto messageDescriptorProto) {
        this.parent = parent;
        this.messageDescriptorProto = messageDescriptorProto;
        messageExtensions = Optional.of(messageDescriptorProto)
                .filter(DescriptorProtos.DescriptorProto::hasOptions)
                .map(DescriptorProtos.DescriptorProto::getOptions)
                .filter(o -> o.hasExtension(Extensions.javaHelper))
                .map(o -> o.getExtension(Extensions.javaHelper))
                .orElse(null);
    }

    @Override
    public Stream<NestedStreamingIterable<PluginProtos.CodeGeneratorResponse.File>> children() {
        // Preserve the invariant that any nested class will always see a non-null `messageExtensions`
        if (messageExtensions == null) {
            return Stream.empty();
        } else {
            return messageDescriptorProto
                    .getFieldList()
                    .stream()
                    .map(this::generateChild);
        }
    }
    private FieldDescriptorModifications generateChild(DescriptorProtos.FieldDescriptorProto fieldDescriptorProto) {
        return new FieldDescriptorModifications(this, fieldDescriptorProto);
    }

}
