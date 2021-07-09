package com.boeckerman.jake.protobuf;

import com.boeckerman.jake.protobuf.Context.FieldContext;
import com.boeckerman.jake.protobuf.Extensions.JavaFieldExtension.NullableOptions;
import com.boeckerman.jake.protobuf.filecoordinates.GeneratedResponseFileCoordinates;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.File;
import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.util.stream.Stream;

import static com.boeckerman.jake.protobuf.CodeGeneratorUtils.CamelCase;
import static com.boeckerman.jake.protobuf.TypeUtils.isPrimitive;
import static com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Label.LABEL_OPTIONAL;

public class NullableFields implements FieldHandler, GetterSetterHelper {
    private final FieldContext fieldContext;
    private final DescriptorProtos.FieldDescriptorProto fieldDescriptorProto;
    private final NullableOptions nullableOptions;
    private final TypeUtils.TypeNames typeNames;
    private final NameVariants.FieldNames nameVariants;

    public NullableFields(FieldContext fieldContext) {
        this.fieldContext = fieldContext;
        this.fieldDescriptorProto = fieldContext.fieldDescriptorProto();
        this.nullableOptions = fieldContext.fieldExtension().getNullable();
        this.typeNames = fieldContext.executionContext().typeNames().lookup(fieldDescriptorProto);
        this.nameVariants = new NameVariants.FieldNames(fieldContext);
    }

    public static String nullableName(FieldContext fieldContext) {
        return _nullableName(fieldContext.fieldDescriptorProto(), fieldContext.fieldExtension().getNullable());
    }

    public static String _nullableName(DescriptorProtos.FieldDescriptorProto fieldDescriptorProto, NullableOptions nullableOptions) {
        boolean primitive = isPrimitive(fieldDescriptorProto.getType());
        return CamelCase(primitive ?
                StringUtils.removeEnd(fieldDescriptorProto.getName(), nullableOptions.getPrimitiveSuffix()) :
                StringUtils.removeEnd(fieldDescriptorProto.getName(), nullableOptions.getObjectSuffix()));
    }

    @Override
    public Stream<File> get() {
        boolean hasObjectSuffix = nameVariants.proto_name().endsWith(nullableOptions.getObjectSuffix());
        boolean hasPrimitiveSuffix = nameVariants.proto_name().endsWith(nullableOptions.getPrimitiveSuffix());

        if (fieldDescriptorProto.getLabel() != LABEL_OPTIONAL) {
            if (hasPrimitiveSuffix || hasObjectSuffix) {
                return warningResponse(MessageFormat.format(
                        "// Heads up! the field {0} isn't optional, but would otherwise be covered by {1}",
                        nameVariants.proto_name(),
                        NullableOptions.class.getName()));
            }
            return Stream.empty();
        }
        if (!(hasPrimitiveSuffix || hasObjectSuffix)) { // no suffix -> no output
            return Stream.empty();
        }

        boolean primitive = typeNames.isPrimitive();
        if (primitive && hasPrimitiveSuffix) {
            return response();
        } else if (!primitive && hasObjectSuffix) {
            return response();
        } else {
            return warningResponse(MessageFormat.format(
                    "// Heads up! the field {0} is {1}, but has the {2} suffix ({3})",
                    nameVariants.proto_name(),
                    (primitive ? "primitive" : "an object"),
                    (!primitive ? "primitive" : "object"),
                    CodeGeneratorUtils.debugPeek(nullableOptions)));
        }
    }

    private Stream<File> response() {
        return Stream.of(has(), getter(), nullableGetter(), nullableSetter());
    }

    // NOT needed for the mixin to compile (the setter gets directly injected into the Builder, which already has this defined.
    private File setter() {
        return builderContext(methodDeclarationHeader("void", "set", nameVariants.protoGeneratedName(), protoType() + " value").append(";").toString());
    }

    // NOT needed for the mixin to compile (the setter gets directly injected into the Builder, which already has this defined.
    private File clearer() {
        return builderContext(methodDeclarationHeader("void", "clear", nameVariants.protoGeneratedName()).append(";").toString());
    }

    private File nullableSetter() {
        return builderContext("""
                %s // nullable field setter, which forwards to traditional builder methods
                {
                    if(value == null) return %s;
                    else return %s;
                }
                """.formatted(
                methodDeclarationHeader("Builder", "set", nameVariants.nullableName(), nullableType() + " value"),
                methodInvoke("clear", nameVariants.protoGeneratedName()),
                methodInvoke("set", nameVariants.protoGeneratedName(), "value")));
    }

    private File nullableGetter() {
        return mixinContext("""
                default %s // nullable field getter which forwards to traditional getters
                {
                    if(%s) return %s;
                    else return null;
                }
                """.formatted(
                methodDeclarationHeader(nullableType(), "get", nameVariants.nullableName()),
                methodInvoke("has", nameVariants.protoGeneratedName()),
                methodInvoke("get", nameVariants.protoGeneratedName())));
    }

    @Override
    public GeneratedResponseFileCoordinates context() {
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
