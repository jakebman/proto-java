package com.boeckerman.jake.protobuf.nested;

import com.boeckerman.jake.protobuf.CodeGeneratorUtils;
import com.boeckerman.jake.protobuf.InsertionPoint.InsertionPointPrefix;
import com.boeckerman.jake.protobuf.nested.contexts.FieldContext;
import com.boeckerman.jake.protobuf.nested.contexts.MessageContext;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.File;

import java.util.stream.Stream;

class FieldDescriptorModifications implements NestedStreamingIterable<File>, FieldContext {
    final MessageDescriptorModifications parent;
    final FieldDescriptorProto fieldDescriptorProto;

    FieldDescriptorModifications(MessageDescriptorModifications parent, DescriptorProtos.FieldDescriptorProto fieldDescriptorProto) {
        this.parent = parent;
        this.fieldDescriptorProto = fieldDescriptorProto;
    }


    @Override
    public Stream<File> stream() {
        if (CodeGeneratorUtils.isPrimitive(fieldDescriptorProto.getType()) && fieldDescriptorProto.getName().endsWith(getNullableOptions().getPrimitiveSuffix())) {
            return Stream.of(fileBuilderFor(InsertionPointPrefix.class_scope)
                    .setContent("//" + this.getClass().getName() + " - Recognize that we need to do something with primitive " + fieldDescriptorProto.getName())
                    .build());
        } else if (!CodeGeneratorUtils.isPrimitive(fieldDescriptorProto.getType()) && fieldDescriptorProto.getName().endsWith(getNullableOptions().getObjectSuffix())) {
            return Stream.of(fileBuilderFor(InsertionPointPrefix.class_scope)
                    .setContent("//" + this.getClass().getName() + " - Recognize Object we need to work on " + fieldDescriptorProto.getName())
                    .build());
        } else {
            return Stream.empty();
        }
    }

    private File.Builder fileBuilderFor(InsertionPointPrefix insertionPointPrefix) {
        return insertionPointPrefix.fileBuilderFor(getFileDescriptorProto(), getMessageDescriptorProto());
    }

    // Context-passing code to help FieldDescriptorModifications read all necessary context
    @Override
    public FieldDescriptorProto getFieldDescriptorProto() {
        return fieldDescriptorProto;
    }

    @Override
    public MessageContext delegate() {
        return parent;
    }
}
