package com.boeckerman.jake.protobuf;

import com.boeckerman.jake.protobuf.filecoordinates.GeneratedResponseFileCoordinates;
import com.boeckerman.jake.protobuf.filecoordinates.GeneratedResponseFileCoordinates.simple;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Type;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorRequest;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TypeUtils {

    public static TypeReference generateLookupTableFor(CodeGeneratorRequest request) {
        return generateLookupTableFor(request.getProtoFileList());
    }

    public static class TypeReference {
        Map<String, TypeNames> lookupTable;

        public TypeReference(Map<String, TypeNames> lookupTable) {
            this.lookupTable = lookupTable;
        }

        public TypeNames lookup(DescriptorProtos.FieldDescriptorProto fieldDescriptorProto) {
            Type type = fieldDescriptorProto.getType();
            TypeNames output = BoxingType.hasType(type) ?
                    new ProtoAndJavaTypeNames(type) :
                    lookupTable.get(fieldDescriptorProto.getTypeName());

            if (output == null) {
                throw new RuntimeException(this.describe() + " does not have " + CodeGeneratorUtils.debugPeek(fieldDescriptorProto));
            }
            return output;
        }

        public String describe() {
            return lookupTable.entrySet()
                    .stream()
                    .filter(e -> !e.getKey().startsWith(".google.protobuf")) // no meta
                    .map(e -> e.getKey() + ":" + e.getValue().describe())
                    .collect(Collectors.joining(",", "LookupTable(", ")"));
        }

        public JavaTypeNames lookupMessageType(String messageName) {
            return lookupTable.get("." + messageName);
        }
    }

    public static TypeReference generateLookupTableFor(Collection<FileDescriptorProto> protoFileList) {
        Map<String, TypeNames> objects = protoFileList
                .stream()
                .flatMap(TypeUtils::allNestedNames)
                .collect(Collectors.toMap(x -> "." + protoTypeName(x), ClassLike::new));
        return new TypeReference(objects);
    }

    static Stream<GeneratedResponseFileCoordinates> allNestedNames(FileDescriptorProto root) {
        return StreamUtil.<GeneratedResponseFileCoordinates>concat(
                root
                        .getMessageTypeList()
                        .stream()
                        .map(descriptorProto -> new simple(root, descriptorProto))
                        .flatMap(TypeUtils::allNestedNames),
                root.getEnumTypeList()
                        .stream()
                        .map(enumDescriptorProto ->
                                HACK_coordinatesForEnumDescriptor(root, enumDescriptorProto.getName()))
        );
    }

    static Stream<GeneratedResponseFileCoordinates> allNestedNames(GeneratedResponseFileCoordinates root) {
        return StreamUtil.<GeneratedResponseFileCoordinates>concat(
                root,
                root.descriptorProto()
                        .getNestedTypeList()
                        .stream()
                        .map(descriptorProto -> new simple(root.fileDescriptorProto(), descriptorProto.toBuilder()
                                .setName(javaClassName(root) + PACKAGE_SEPERATOR + descriptorProto.getName()).build())) // TODO: does the nested message know its proper name?
                        .flatMap(TypeUtils::allNestedNames),
                root.descriptorProto()
                        .getEnumTypeList()
                        .stream()
                        .map(enumDescriptorProto ->
                                HACK_coordinatesForEnumDescriptor(root.fileDescriptorProto(),
                                        javaClassName(root) + PACKAGE_SEPERATOR + enumDescriptorProto.getName())));
    }

    // TODO: unwise shim, to allow protoTypeName to work
    private static simple HACK_coordinatesForEnumDescriptor(FileDescriptorProto fileDescriptorProto, String name) {
        return new simple(fileDescriptorProto,
                DescriptorProto.newBuilder()
                        .setName(name)
                        .build());
    }

    public static String PACKAGE_SEPERATOR = ".";

    public static String protoTypeName(GeneratedResponseFileCoordinates coordinates) {
        String messageDescriptorTypename = coordinates.descriptorProto().getName();
        FileDescriptorProto fileDescriptorProto = coordinates.fileDescriptorProto();

        if (fileDescriptorProto.hasPackage()) {
            return fileDescriptorProto.getPackage() + PACKAGE_SEPERATOR + messageDescriptorTypename;
        }
        return messageDescriptorTypename;
    }

    public static String javaFullClassName(GeneratedResponseFileCoordinates messageContext) {
        StringBuilder out = new StringBuilder(javaPackage(messageContext));

        if (out.length() > 0) {
            out.append(TypeUtils.PACKAGE_SEPERATOR);
        }

        out.append(javaClassName(messageContext.descriptorProto()));

        return out.toString();
    }

    public static String javaPackage(GeneratedResponseFileCoordinates messageContext) {
        DescriptorProtos.FileOptions options = messageContext.fileDescriptorProto().getOptions();

        if (options.hasJavaPackage()) {
            return options.getJavaPackage();
        } else if (messageContext.fileDescriptorProto().hasPackage()) {
            return messageContext.fileDescriptorProto().getPackage();
        } else {
            return "";
        }
    }


    public static String javaClassName(GeneratedResponseFileCoordinates fileCoordinates) {
        return javaClassName(fileCoordinates.descriptorProto());
    }

    public static String javaClassName(DescriptorProto descriptorProto) {
        return CodeGeneratorUtils.CamelCase(descriptorProto.getName());
    }

    static record ProtoAndJavaTypeNames(BoxingType boxingType, String proto_type_name) implements TypeNames {
        public ProtoAndJavaTypeNames(Type type) {
            this(BoxingType.fromType(type), textName(type));
        }

        public static String textName(Type type) {
            return StringUtils.removeStart(type.name(), "TYPE_").toLowerCase();
        }

        @Override
        public String primitive() {
            return boxingType.primitive();
        }

        @Override
        public String boxed() {
            return boxingType.boxed();
        }

        @Override
        public boolean isPrimitive() {
            return boxingType.isPrimitive(); // very likely always true, but this is delegation
        }
    }


    public enum BoxingType implements JavaTypeNames {
        Integer("int"),
        Double("double"),
        Float("float"),
        Boolean("boolean"),
        Long("long"),

        String("java.lang.String") {
            public boolean isPrimitive() {
                return false;
            }

            public String boxed() {
                return primitiveName;
            }
        },
        Bytes("com.google.protobuf.ByteString") {
            public boolean isPrimitive() {
                return false;
            }

            public String boxed() {
                return primitiveName;
            }
        };


        public final String primitiveName;

        BoxingType(String primitiveName) {
            this.primitiveName = primitiveName;
        }

        @Override
        public String primitive() {
            return primitiveName;
        }

        @Override
        public String boxed() {
            return name();
        }

        @Override
        public boolean isPrimitive() {
            return true;
        }


        static boolean hasType(Type type) {
            return fromType(type) != null;
        }

        static BoxingType fromType(Type type) {
            return switch (type) {
                case TYPE_DOUBLE -> Double;
                case TYPE_FLOAT -> Float;
                case TYPE_FIXED64, TYPE_SFIXED64, // fixed-point, wide
                        TYPE_INT64, TYPE_UINT64, TYPE_SINT64 // ints, wide
                        -> Long;
                case TYPE_FIXED32, TYPE_SFIXED32,  // fixed-point
                        TYPE_INT32, TYPE_UINT32, TYPE_SINT32 // int
                        -> Integer;
                case TYPE_BOOL -> Boolean;
                case TYPE_STRING -> String;
                case TYPE_BYTES -> Bytes;
                case TYPE_GROUP, TYPE_MESSAGE, TYPE_ENUM -> null;
                default -> null;// TODO: log an error
            };
        }
    }

    interface JavaTypeNames {
        String primitive();

        String boxed();

        boolean isPrimitive();

        default String describe() {
            return "" +
                    "primitive='" + primitive() + "'," +
                    "boxed='" + boxed() + "'" +
                    "";
        }
    }

    interface TypeNames extends JavaTypeNames {
        String proto_type_name();

        default String describe() {
            String primitive = primitive();
            String boxed = boxed();
            String proto = proto_type_name();
            if (StringUtils.equals(primitive, boxed)) {
                if (StringUtils.equals(boxed, proto)) {
                    return "Typename=" + proto;
                } else {
                    return "TypeName(" +
                            "java='" + boxed + "'," +
                            "proto_='" + proto + "')";
                }
            }
            return "TypeName(" +
                    "primitive='" + primitive + "'," +
                    "boxed='" + boxed + "'," +
                    "proto_='" + proto + "'" +
                    ")";
        }
    }

    public static boolean isPrimitive(Type type) {
        return switch (type) {
            case TYPE_DOUBLE, TYPE_FLOAT, // floating-point
                    TYPE_FIXED64, TYPE_SFIXED64, // fixed-point, wide
                    TYPE_INT64, TYPE_UINT64, TYPE_SINT64, // ints, wide
                    TYPE_FIXED32, TYPE_SFIXED32,  // fixed-point
                    TYPE_INT32, TYPE_UINT32, TYPE_SINT32, // int
                    TYPE_BOOL -> true;
            case TYPE_STRING, TYPE_GROUP, TYPE_MESSAGE, TYPE_BYTES, TYPE_ENUM -> false;
            // Protobuf is allowed to add new Enums to this class. In that case, java throws this error, but without a message
            // https://docs.oracle.com/javase/specs/jls/se16/html/jls-15.html#jls-15.28.2
            default -> throw new IncompatibleClassChangeError(type + " was not a valid value at compile time");
        };
    }

    static record ClassLike(String javaName, String proto_type_name) implements TypeNames {
        public ClassLike(GeneratedResponseFileCoordinates protoIdentifier) {
            this(javaFullClassName(protoIdentifier), protoTypeName(protoIdentifier));
        }

        @Override
        public String primitive() {
            return javaName;
        }

        @Override
        public String boxed() {
            return javaName;
        }

        @Override
        public boolean isPrimitive() {
            return false;
        }

        @Override
        public String describe() {
            return "" +
                    "java='" + javaName() + "'," +
                    "proto_='" + proto_type_name() + "'" +
                    "";
        }
    }
}
