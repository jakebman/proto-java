package com.boeckerman.jake.protobuf;

import com.boeckerman.jake.protobuf.filecoordinates.GeneratedResponseFileCoordinates;

import static com.boeckerman.jake.protobuf.CodeGeneratorUtils.CamelCase;

public interface NameVariants {
    // the preferred name
    String name();

    String proto_name();

    record FieldNames(
            String proto_name, // looks like original_name_from_proto_file_{some_suffix}
            String protoGeneratedName, // looks like OriginalNameFromProtoFile{SomeSuffix}; compatible with getX prefixing
            String nullableName // looks like OriginalNameFromProtoFile; will be identical to protoGeneratedName if NullableFields doesn't apply
    ) implements NameVariants {
        public FieldNames(Context.FieldContext fieldContext) {
            this(fieldContext.fieldDescriptorProto().getName(),
                    CamelCase(fieldContext.fieldDescriptorProto().getName()), // todo: pontentially wrong
                    NullableFields.nullableName(fieldContext));
        }

        // the preferred name to use in this plugin -
        // since nullableName will only be different from protoGeneratedName if NullableFields generates it,
        // we can safely assume this name is correct.
        @Override
        public String name() {
            return nullableName();
        }
    }
}
