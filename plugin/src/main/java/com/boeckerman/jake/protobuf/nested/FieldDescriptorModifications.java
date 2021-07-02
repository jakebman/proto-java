package com.boeckerman.jake.protobuf.nested;

import com.boeckerman.jake.protobuf.CodeGeneratorUtils;
import com.boeckerman.jake.protobuf.Extensions;
import com.boeckerman.jake.protobuf.InsertionPoint;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.compiler.PluginProtos;

import java.util.stream.Stream;

class FieldDescriptorModifications implements NestedStreamingIterable<PluginProtos.CodeGeneratorResponse.File> {
    private final MessageDescriptorModifications parent;
    final DescriptorProtos.FieldDescriptorProto fieldDescriptorProto;
    final Extensions.JavaExtensionOptions.NullableOptions nullableOptions;

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
}
