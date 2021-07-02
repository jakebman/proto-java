package com.boeckerman.jake.protobuf.nested;

import com.boeckerman.jake.protobuf.Extensions;
import com.boeckerman.jake.protobuf.nested.contexts.FileContext;
import com.boeckerman.jake.protobuf.nested.contexts.MessageContext;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.compiler.PluginProtos;

import java.util.Optional;
import java.util.stream.Stream;

class MessageDescriptorModifications implements NestedStreamingIterable<PluginProtos.CodeGeneratorResponse.File>, MessageContext {
    final FileDescriptorModifications parent;
    final DescriptorProtos.DescriptorProto messageDescriptorProto;
    final Extensions.JavaExtensionOptions messageExtensions;

    public MessageDescriptorModifications(FileDescriptorModifications parent, DescriptorProtos.DescriptorProto messageDescriptorProto) {
        this.parent = parent;
        this.messageDescriptorProto = messageDescriptorProto;
        messageExtensions = Optional.of(messageDescriptorProto)
                .filter(descriptorProto -> getInvocationParameters().run_everywhere || descriptorProto.hasOptions())
                .map(DescriptorProtos.DescriptorProto::getOptions)
                .filter(o -> getInvocationParameters().run_everywhere || o.hasExtension(Extensions.javaHelper))
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

    // Context-passing code to help FieldDescriptorModifications read all necessary context
    @Override
    public DescriptorProtos.DescriptorProto getMessageDescriptorProto() {
        return messageDescriptorProto;
    }

    @Override
    public Extensions.JavaExtensionOptions getMessageExtensions() {
        return messageExtensions;
    }

    @Override
    public FileContext delegate() {
        return parent;
    }
}
