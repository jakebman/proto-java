package com.boeckerman.jake.protobuf.domains;

import com.google.protobuf.DescriptorProtos;

public class Types {
    public static boolean isPrimitive(DescriptorProtos.FieldDescriptorProto.Type type) {
        switch (type) {
            case TYPE_DOUBLE:
            case TYPE_FLOAT:
            case TYPE_INT64:
            case TYPE_UINT64:
            case TYPE_INT32:
            case TYPE_FIXED64:
            case TYPE_FIXED32:
            case TYPE_BOOL: // nb: DOUBLE->BOOL is a block of contiguous ids, then the rest of these are intermingled
            case TYPE_UINT32:
            case TYPE_SFIXED32:
            case TYPE_SFIXED64:
            case TYPE_SINT32:
            case TYPE_SINT64:
                return true;
            case TYPE_STRING:
            case TYPE_GROUP: // TOOD: WTF
            case TYPE_MESSAGE:
            case TYPE_BYTES:
            case TYPE_ENUM:
                return false;
            default:
                throw new RuntimeException("New type added, unrecognized");
        }
    }
}
