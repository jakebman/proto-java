package com.boeckerman.jake.protobuf;

import com.boeckerman.jake.protobuf.Context.FieldContext;
import com.boeckerman.jake.protobuf.filecoordinates.InsertionPoint;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.File;
import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.boeckerman.jake.protobuf.CodeGeneratorUtils.CamelCase;
import static com.boeckerman.jake.protobuf.CodeGeneratorUtils.isPrimitive;
import static com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Label.LABEL_OPTIONAL;

public class NullableFields implements Supplier<Stream<File>> {
    private final String nullableName;
    private final FieldContext fieldContext;
    private final DescriptorProtos.FieldDescriptorProto fieldDescriptorProto;
    private final Extensions.JavaFieldExtension.NullableOptions nullableOptions;
    private final boolean primitive;

    private final String protoName;

    public NullableFields(FieldContext fieldContext) {
        this.fieldContext = fieldContext;
        this.fieldDescriptorProto = fieldContext.fieldDescriptorProto();
        this.nullableOptions = fieldContext.fieldExtension().getNullable();
        this.primitive = isPrimitive(fieldDescriptorProto.getType());
        this.protoName = CamelCase(fieldDescriptorProto.getName());

        this.nullableName = CamelCase(primitive ?
                StringUtils.removeEnd(fieldDescriptorProto.getName(), nullableOptions.getPrimitiveSuffix()) :
                StringUtils.removeEnd(fieldDescriptorProto.getName(), nullableOptions.getObjectSuffix()));
    }

    @Override
    public Stream<File> get() {
        //nb: can't use protoName here - it's been CamelCase`d
        boolean hasObjectSuffix = fieldDescriptorProto.getName().endsWith(nullableOptions.getObjectSuffix());
        boolean hasPrimitiveSuffix = fieldDescriptorProto.getName().endsWith(nullableOptions.getPrimitiveSuffix());

        if (fieldDescriptorProto.getLabel() != LABEL_OPTIONAL) {
            if (hasPrimitiveSuffix || hasObjectSuffix) {
                return warningResponse(MessageFormat.format(
                        "// Heads up! the field {0} isn't optional, but would otherwise be covered by {1}",
                        fieldDescriptorProto.getName(),
                        Extensions.JavaFieldExtension.NullableOptions.class.getName()));
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
                    fieldDescriptorProto.getName(),
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
        return mixinContext(methodDeclarationHeader("boolean", "has", protoName).append(";").toString());
    }

    // needed for the mixin to compile. Both the Builder and the Message already have this defined
    private File getter() {
        return mixinContext(methodDeclarationHeader(protoType(), "get", protoName).append(";").toString());
    }

    // needed for the mixin to compile. Both the Builder and the Message already have this defined
    private File setter() {
        return builderContext(methodDeclarationHeader("void", "set", protoName, protoType() + " value").append(";").toString());
    }

    // needed for the mixin to compile. Both the Builder and the Message already have this defined
    private File clearer() {
        return builderContext(methodDeclarationHeader("void", "clear", protoName).append(";").toString());
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
                methodInvoke("clear", protoName),
                methodInvoke("set", protoName, "value")));
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
                methodInvoke("has", protoName),
                methodInvoke("get", protoName)));
    }

    private String nullableType() {
        String type;
        if (primitive) {
            type = CodeGeneratorUtils.BoxingType.fromType(fieldDescriptorProto.getType()).toString();
        } else {
            type = fieldDescriptorProto.getTypeName();// TODO: likely not going to work due to type name impedence between proto and java
        }
        return type;
    }

    private String protoType() {
        String type;
        if (primitive) {
            type = CodeGeneratorUtils.BoxingType.fromType(fieldDescriptorProto.getType()).primitiveName;
        } else {
            type = fieldDescriptorProto.getTypeName();// TODO: likely not going to work due to type name impedence between proto and java
        }
        return type;
    }


    private Stream<File> warningResponse(String content) {
        return Stream.of(mixinContext(content));
    }

    private File mixinContext(String content) {
        return fieldContext
                .fileBuilderFor(InsertionPoint.custom_mixin_interface_scope)
                .setContent(content)
                .build();
    }

    private File builderContext(String content) {
        return fieldContext
                .fileBuilderFor(InsertionPoint.builder_scope)
                .setContent(content)
                .build();
    }


    // String getFoo()
    // Object flyBar(String one, String two)
    static StringBuilder methodDeclarationHeader(Object type, String verb, String fieldName, String... args) {
        StringBuilder out = new StringBuilder();
        out.append(type);
        out.append(" ");
        methodInvoke(verb, fieldName, out, args);
        return out;
    }

    private static StringBuilder methodInvoke(String verb, String fieldName, String... args) {
        return methodInvoke(verb, fieldName, new StringBuilder(), args);
    }

    // flyBar(String one, String two)
    // OR
    // setBar(one, two)
    private static StringBuilder methodInvoke(String verb, String fieldName, StringBuilder out, String... args) {
        return methodName(verb, fieldName, out)
                .append(Arrays.stream(args).collect(Collectors.joining(",", "(", ")")));
    }

    private static StringBuilder methodName(String verb, String fieldName, StringBuilder out) {
        out.append(verb);
        out.append(fieldName);
        return out;
    }

}
