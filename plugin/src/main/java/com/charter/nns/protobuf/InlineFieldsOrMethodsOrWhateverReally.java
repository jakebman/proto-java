package com.charter.nns.protobuf;

import com.charter.nns.protobuf.Context.FieldContext;
import com.charter.nns.protobuf.Extensions.JavaFieldExtension.InlineCodeOptions;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.File;

import java.util.stream.Stream;

public class InlineFieldsOrMethodsOrWhateverReally implements FieldHandler, GetterSetterHelper {
    private final FieldContext fieldContext;
    private final DescriptorProtos.FieldDescriptorProto fieldDescriptorProto;
    private final InlineCodeOptions inlineCode;
    private final TypeUtils.TypeNames typeNames;
    private final NameVariants.FieldNames nameVariants;

    public InlineFieldsOrMethodsOrWhateverReally(FieldContext fieldContext) {
        this.fieldContext = fieldContext;
        this.fieldDescriptorProto = fieldContext.fieldDescriptorProto();
        this.inlineCode = fieldContext.fieldExtension().getInline();
        this.typeNames = fieldContext.executionContext().typeNames().lookup(fieldDescriptorProto);
        this.nameVariants = new NameVariants.FieldNames(fieldContext);
    }

    @Override
    public Stream<File> get() {
        return inlineCode.getCodeList().stream().map(this::classContext);
    }

    @Override
    public FieldContext context() {
        return fieldContext;
    }

    @Override
    public NameVariants.FieldNames nameVariants() {
        return nameVariants;
    }

    @Override
    public TypeUtils.TypeNames typeNames() {
        return typeNames;
    }
}
