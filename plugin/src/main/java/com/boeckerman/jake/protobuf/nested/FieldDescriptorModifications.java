package com.boeckerman.jake.protobuf.nested;

import com.boeckerman.jake.protobuf.CodeGeneratorUtils;
import com.boeckerman.jake.protobuf.Extensions.JavaExtensionOptions.NullableOptions;
import com.boeckerman.jake.protobuf.InsertionPoint;
import com.boeckerman.jake.protobuf.nested.contexts.FieldContext;
import com.boeckerman.jake.protobuf.nested.contexts.MessageContext;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.File;

import java.util.stream.Stream;

class FieldDescriptorModifications implements NestedStreamingIterable<File>, FieldContext {
    final MessageDescriptorModifications parent;
    final FieldDescriptorProto fieldDescriptorProto;
    final NullableOptions nullableOptions;

    FieldDescriptorModifications(MessageDescriptorModifications parent, DescriptorProtos.FieldDescriptorProto fieldDescriptorProto) {
        this.parent = parent;
        this.fieldDescriptorProto = fieldDescriptorProto;
        this.nullableOptions = parent.messageExtensions.getNullableOptionals();
    }

    @Override
    public Stream<PluginProtos.CodeGeneratorResponse.File> stream() {
        if (CodeGeneratorUtils.isPrimitive(fieldDescriptorProto.getType()) && fieldDescriptorProto.getName().endsWith(nullableOptions.getPrimitiveSuffix())) {
            return Stream.of(InsertionPoint.class_scope
                    // TODO: parent.parent is very gross. A better way?
                    .fileBuilderFor(parent.parent.fileDescriptorProto, parent.messageDescriptorProto)
                    .setContent("//" + this.getClass().getName() + " - Recognize that we need to do something with primitive " + fieldDescriptorProto.getName())
                    .build());
        } else if (!CodeGeneratorUtils.isPrimitive(fieldDescriptorProto.getType()) && fieldDescriptorProto.getName().endsWith(nullableOptions.getObjectSuffix())) {
            return Stream.of(InsertionPoint.class_scope.fileBuilderFor(parent.parent.fileDescriptorProto, parent.messageDescriptorProto)
                    .setContent("//" + this.getClass().getName() + " - Recognize Object we need to work on " + fieldDescriptorProto.getName())
                    .build());
        } else {
            return Stream.empty();
        }
    }

    // Context-passing code to help FieldDescriptorModifications read all necessary context
    @Override
    public FieldDescriptorProto getFieldDescriptorProto() {
        return fieldDescriptorProto;
    }

    @Override
    public NullableOptions getNullableOptions() {
        return nullableOptions;
    }

    @Override
    public MessageContext delegate() {
        return parent;
    }
}
