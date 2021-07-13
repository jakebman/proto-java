package com.charter.nns.protobuf;

import org.apache.commons.lang3.StringUtils;

import static com.charter.nns.protobuf.CodeGeneratorUtils.CamelCase;

public interface NameVariants {
    // the preferred name
    String name();

    String proto_name();

    record FieldNames(
            // looks like original_name_from_proto_file_{some_suffix}
            String proto_name,
            // looks like OriginalNameFromProtoFile{SomeSuffix}; compatible with getX prefixing
            String protoGeneratedName,
            // looks like OriginalNameFromProtoFile; will be identical to protoGeneratedName if NullableFields doesn't apply
            String nullableName)
            implements NameVariants {
        public FieldNames(Context.FieldContext fieldContext) {
            this(fieldContext.fieldDescriptorProto().getName(),
                    CamelCase(fieldContext.fieldDescriptorProto().getName()), // todo: potentially wrong
                    NullableFields.nullableName(fieldContext));
        }

        public boolean hasNullable() {
            return !StringUtils.equals(protoGeneratedName, nullableName);
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
