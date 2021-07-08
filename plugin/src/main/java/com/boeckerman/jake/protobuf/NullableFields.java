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
    private final boolean primitive;
    private final TypeUtils.JavaTypeNames javaTypeNames;

    private final String original_name_from_proto_file; // looks like original_name_from_proto_file_{some_suffix}
    private final String protoGeneratedName; // looks like OriginalNameFromProtoFile{SomeSuffix}
    private final String nullableName; // looks like OriginalNameFromProtoFile

    public NullableFields(FieldContext fieldContext) {
        this.fieldContext = fieldContext;
        this.fieldDescriptorProto = fieldContext.fieldDescriptorProto();
        this.nullableOptions = fieldContext.fieldExtension().getNullable();
        this.primitive = isPrimitive(fieldDescriptorProto.getType());
        this.javaTypeNames = fieldContext.executionContext().typeNames().apply(fieldDescriptorProto);
        this.original_name_from_proto_file = fieldDescriptorProto.getName();
        this.protoGeneratedName = CamelCase(original_name_from_proto_file);

        this.nullableName = CamelCase(primitive ?
                StringUtils.removeEnd(original_name_from_proto_file, nullableOptions.getPrimitiveSuffix()) :
                StringUtils.removeEnd(original_name_from_proto_file, nullableOptions.getObjectSuffix()));
    }

    @Override
    public Stream<File> get() {
        boolean hasObjectSuffix = original_name_from_proto_file.endsWith(nullableOptions.getObjectSuffix());
        boolean hasPrimitiveSuffix = original_name_from_proto_file.endsWith(nullableOptions.getPrimitiveSuffix());

        if (fieldDescriptorProto.getLabel() != LABEL_OPTIONAL) {
            if (hasPrimitiveSuffix || hasObjectSuffix) {
                return warningResponse(MessageFormat.format(
                        "// Heads up! the field {0} isn't optional, but would otherwise be covered by {1}",
                        original_name_from_proto_file,
                        NullableOptions.class.getName()));
            }
            return Stream.empty();
        }
        if (!(hasPrimitiveSuffix || hasObjectSuffix)) { // no suffix -> no output
            return Stream.empty();
        }

        if (primitive && hasPrimitiveSuffix) {
            return response();
        } else if (!primitive && hasObjectSuffix) {
            return response();
        } else {
            return warningResponse(MessageFormat.format(
                    "// Heads up! the field {0} is {1}, but has the {2} suffix ({3})",
                    original_name_from_proto_file,
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
        return mixinContext(methodDeclarationHeader("boolean", "has", protoGeneratedName).append(";").toString());
    }

    // needed for the mixin to compile. Both the Builder and the Message already have this defined
    private File getter() {
        return mixinContext(methodDeclarationHeader(protoType(), "get", protoGeneratedName).append(";").toString());
    }

    // NOT needed for the mixin to compile (the setter gets directly injected into the Builder, which already has this defined.
    private File setter() {
        return builderContext(methodDeclarationHeader("void", "set", protoGeneratedName, protoType() + " value").append(";").toString());
    }

    // NOT needed for the mixin to compile (the setter gets directly injected into the Builder, which already has this defined.
    private File clearer() {
        return builderContext(methodDeclarationHeader("void", "clear", protoGeneratedName).append(";").toString());
    }

    private File nullableSetter() {
        return builderContext("""
                %s // nullable field setter, which forwards to traditional builder methods
                {
                    if(value == null) %s;
                    else %s;
                }
                """.formatted(
                methodDeclarationHeader("void", "set", nullableName, nullableType() + " value"),
                methodInvoke("clear", protoGeneratedName),
                methodInvoke("set", protoGeneratedName, "value")));
    }

    private File nullableGetter() {
        return mixinContext("""
                default %s // nullable field getter which forwards to traditional getters
                {
                    if(%s) return %s;
                    else return null;
                }
                """.formatted(
                methodDeclarationHeader(nullableType(), "get", nullableName),
                methodInvoke("has", protoGeneratedName),
                methodInvoke("get", protoGeneratedName)));
    }

    private String nullableType() {
        return javaTypeNames.boxed();
    }

    private String protoType() {
        return javaTypeNames.primitive();
    }


    @Override
    public GeneratedResponseFileCoordinates context() {
        return fieldContext;
    }
}
