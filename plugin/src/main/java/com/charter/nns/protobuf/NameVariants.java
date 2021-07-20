package com.charter.nns.protobuf;

import org.apache.commons.lang3.StringUtils;

import static com.charter.nns.protobuf.CodeGeneratorUtils.CamelCase;

public interface NameVariants {

    String proto_name();

    String protoMangledName();

    // the preferred name
    String name();


    record FieldNames(
            // Below, {AppropriateSuffix} stands in for the user's value for the appropriate suffix value
            // that's either NullableOptions.primitveSuffix() or NullableOptions.objectSuffix(); based on whether this field is primitive or not
            // Or, it is the empty string if those suffixes do not match the name of this field.
            // The suffix *will* be part of the field described as long as it appears in the 'like' statement (It's removed in nullableName)
            // The {OptionalProto-generatedListSuffix} is added by protoc to the field's name when the field is a list.

            // looks like original_name_from_proto_file{_appropriate_suffix}
            String proto_name,
            // looks like OriginalNameFromProtoFile{AppropriateSuffix}{OptionalProto-generatedListSuffix}; compatible with getX prefixing
            String protoMangledName,
            // looks like OriginalNameFromProtoFile; will be identical to protoMangledName if NullableFields doesn't apply and ListOptions.friendly_getter is disabled
            String nullableName)
            implements NameVariants {
        public FieldNames(Context.FieldContext fieldContext) {
            this(fieldContext.fieldDescriptorProto().getName(),
                    getProtoGeneratedName(fieldContext),
                    getNullableName(fieldContext));
        }

        public boolean hasNullable() {
            return !StringUtils.equals(protoMangledName, nullableName);
        }

        // the preferred name to use in this plugin -
        // since nullableName will only be different from protoMangledName if NullableFields generates it,
        // we can safely assume this name is correct.
        @Override
        public String name() {
            return nullableName();
        }
    }

    static String getProtoGeneratedName(Context.FieldContext fieldContext) {
        String name = CamelCase(fieldContext.fieldDescriptorProto().getName());
        if (FieldHandler.isList(fieldContext)) {
            return name + "List";
        }
        return name;
    }

    static String getNullableName(Context.FieldContext fieldContext) {
        return NullableFields.nullableName(fieldContext);
    }
}
