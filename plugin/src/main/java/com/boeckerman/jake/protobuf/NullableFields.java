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

public class NullableFields implements FieldHandler {
    private final FieldContext fieldContext;
    private final DescriptorProtos.FieldDescriptorProto fieldDescriptorProto;
    private final NullableOptions nullableOptions;
    private final TypeUtils.TypeNames typeNames;
    private final NameVariants nameVariants;

    public NullableFields(FieldContext fieldContext) {
        this.fieldContext = fieldContext;
        this.fieldDescriptorProto = fieldContext.fieldDescriptorProto();
        this.nullableOptions = fieldContext.fieldExtension().getNullable();
        this.typeNames = fieldContext.executionContext().typeNames().apply(fieldDescriptorProto);
        this.nameVariants = new NameVariants(fieldContext);
    }

    public static String nullableName(FieldContext fieldContext) {
        DescriptorProtos.FieldDescriptorProto fieldDescriptorProto = fieldContext.fieldDescriptorProto();
        NullableOptions nullableOptions = fieldContext.fieldExtension().getNullable();
        boolean primitive = isPrimitive(fieldDescriptorProto.getType());
        return CamelCase(primitive ?
                StringUtils.removeEnd(fieldDescriptorProto.getName(), nullableOptions.getPrimitiveSuffix()) :
                StringUtils.removeEnd(fieldDescriptorProto.getName(), nullableOptions.getObjectSuffix()));
    }

    @Override
    public Stream<File> get() {
        boolean hasObjectSuffix = nameVariants.original_name_from_proto_file().endsWith(nullableOptions.getObjectSuffix());
        boolean hasPrimitiveSuffix = nameVariants.original_name_from_proto_file().endsWith(nullableOptions.getPrimitiveSuffix());

        if (fieldDescriptorProto.getLabel() != LABEL_OPTIONAL) {
            if (hasPrimitiveSuffix || hasObjectSuffix) {
                return warningResponse(MessageFormat.format(
                        "// Heads up! the field {0} isn't optional, but would otherwise be covered by {1}",
                        nameVariants.original_name_from_proto_file(),
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
                    nameVariants.original_name_from_proto_file(),
                    (primitive ? "primitive" : "an object"),
                    (!primitive ? "primitive" : "object"),
                    CodeGeneratorUtils.debugPeek(nullableOptions)));
        }
    }

    private Stream<File> response() {
        return Stream.of(has(), getter(), nullableGetter(), nullableSetter());
    }

    // needed for the mixin to compile. Both the Builder and the Message already have this defined
    private File has() {
        return mixinContext(methodDeclarationHeader("boolean", "has", nameVariants.protoGeneratedName()).append(";").toString());
    }

    // needed for the mixin to compile. Both the Builder and the Message already have this defined
    private File getter() {
        return mixinContext(methodDeclarationHeader(protoType(), "get", nameVariants.protoGeneratedName()).append(";").toString());
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
                    if(value == null) %s;
                    else %s;
                }
                """.formatted(
                methodDeclarationHeader("void", "set", nameVariants.nullableName(), nullableType() + " value"),
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

    private String nullableType() {
        return typeNames.boxed();
    }

    private String protoType() {
        return typeNames.primitive();
    }


    @Override
    public GeneratedResponseFileCoordinates context() {
        return fieldContext;
    }
}
