package com.boeckerman.jake.protobuf;

import com.boeckerman.jake.protobuf.filecoordinates.GeneratedResponseFileCoordinates;
import com.boeckerman.jake.protobuf.filecoordinates.GeneratedResponseFileCoordinates.simple;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Type;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorRequest;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

public class TypeUtils {

    public static final Map<String, JavaTypeNames> PRIMITIVES = Collections.unmodifiableMap(
            Arrays.stream(Type.values())
                    .filter(TypeUtils::isPrimitive)
                    .collect(Collectors.toMap(TypeUtils::textName, BoxingType::fromType)));

    public static Map<String, JavaTypeNames> generateLookupTableFor(CodeGeneratorRequest request) {
        return generateLookupTableFor(request.getProtoFileList());
    }

    public static Map<String, JavaTypeNames> generateLookupTableFor(Collection<DescriptorProtos.FileDescriptorProto> protoFileList) {
        Map<String, JavaTypeNames> objects = protoFileList
                .stream()
                .flatMap(fileDescriptorProto -> fileDescriptorProto
                        .getMessageTypeList()
                        .stream()
                        .map(descriptorProto ->
                                new simple(fileDescriptorProto, descriptorProto)))
                .collect(Collectors.toMap(TypeUtils::protoTypeName, TypeUtils::messageTypeNames));
        objects.putAll(PRIMITIVES);
        return objects;
    }

    private static String textName(Type type) {
        return StringUtils.removeStart(type.name(), "TYPE_").toLowerCase();
    }

    static JavaTypeNames messageTypeNames(GeneratedResponseFileCoordinates protoIdentifier) {
        return new ClassLike(javaFullClassName(protoIdentifier));
    }

    public static String PACKAGE_SEPERATOR = ".";

    public static String protoTypeName(GeneratedResponseFileCoordinates coordinates) {
        String messageDescriptorTypename = coordinates.descriptorProto().getName();
        DescriptorProtos.FileDescriptorProto fileDescriptorProto = coordinates.fileDescriptorProto();

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
        return javaClassName(fileCoordinates);
    }

    public static String javaClassName(DescriptorProto descriptorProto) {
        return CodeGeneratorUtils.CamelCase(descriptorProto.getName());
    }

    public enum BoxingType implements JavaTypeNames {
        Integer("int"),
        Double("double"),
        Float("float"),
        Boolean("boolean"),
        Long("long");


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
                default -> throw new UnsupportedOperationException("Cannot get Boxing Type for non-primitive " + type);
            };
        }
    }

    interface JavaTypeNames {
        String primitive();

        String boxed();

        default String describe() {
            return "" +
                    "primitive='" + primitive() + "'," +
                    "boxed='" + boxed() + '\'' +
                    "";
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

    static record ClassLike(String classname) implements JavaTypeNames {

        @Override
        public String primitive() {
            return classname;
        }

        @Override
        public String boxed() {
            return classname;
        }

        @Override
        public String describe() {
            return "alwaysNamed=" + classname;
        }

    }
}
