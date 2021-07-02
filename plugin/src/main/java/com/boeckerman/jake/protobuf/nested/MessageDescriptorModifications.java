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
    final Extensions.JavaExtensionOptions.NullableOptions nullableOptions;

    public MessageDescriptorModifications(FileDescriptorModifications parent, DescriptorProtos.DescriptorProto messageDescriptorProto) {
        this.parent = parent;
        this.messageDescriptorProto = messageDescriptorProto;
        Optional<Extensions.JavaExtensionOptions> optionalExtension = Optional.of(messageDescriptorProto)
                .filter(DescriptorProtos.DescriptorProto::hasOptions)
                .map(DescriptorProtos.DescriptorProto::getOptions)
                .filter(o -> o.hasExtension(Extensions.javaHelper))
                .map(o -> o.getExtension(Extensions.javaHelper));


        boolean enableMessageExtensions = optionalExtension
                .map(Extensions.JavaExtensionOptions::getEnabled) // use the enabled flag if truly specified
                .orElse(getInvocationParameters().run_everywhere);
        boolean enableNullable = optionalExtension
                .filter(Extensions.JavaExtensionOptions::hasNullableOptionals)
                .map(Extensions.JavaExtensionOptions::getNullableOptionals)
                .map(Extensions.JavaExtensionOptions.NullableOptions::getEnabled) // use the enabled flag if truly specified
                .orElse(getInvocationParameters().run_everywhere);

        messageExtensions = messageDescriptorProto
                .getOptions()
                .getExtension(Extensions.javaHelper)
                .toBuilder()
                .setEnabled(enableMessageExtensions)
                .build();
        nullableOptions = messageExtensions
                .getNullableOptionals()
                .toBuilder()
                .setEnabled(enableNullable)
                .build();
    }

    @Override
    public Stream<NestedStreamingIterable<PluginProtos.CodeGeneratorResponse.File>> children() {
        // Preserve the invariant that any nested class will always see an enabled nullable options and messageExtensions
        if (nullableOptions.getEnabled() && messageExtensions.getEnabled()) {
            return messageDescriptorProto
                    .getFieldList()
                    .stream()
                    .map(this::generateChild);
        }
        return Stream.empty();
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
    public Extensions.JavaExtensionOptions.NullableOptions getNullableOptions() {
        return nullableOptions;
    }

    @Override
    public FileContext delegate() {
        return parent;
    }
}
