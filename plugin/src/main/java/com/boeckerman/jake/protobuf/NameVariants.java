package com.boeckerman.jake.protobuf;

import com.google.protobuf.DescriptorProtos;

import static com.boeckerman.jake.protobuf.CodeGeneratorUtils.CamelCase;

public record NameVariants(
        String original_name_from_proto_file, // looks like original_name_from_proto_file_{some_suffix}
        String protoGeneratedName, // looks like OriginalNameFromProtoFile{SomeSuffix}
        String nullableName // looks like OriginalNameFromProtoFile; will be identical to protoGeneratedName if NullableFields doesn't apply
) {
    public NameVariants(Context.FieldContext fieldContext) {
        this(fieldContext.fieldDescriptorProto().getName(),
                CamelCase(fieldContext.fieldDescriptorProto().getName()),
                NullableFields.nullableName(fieldContext));
    }
    // the preferred name
    public String name() {
        return nullableName();
    }
}