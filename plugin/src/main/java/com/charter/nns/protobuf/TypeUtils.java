package com.charter.nns.protobuf;

import com.charter.nns.protobuf.filecoordinates.GeneratedResponseFileCoordinates;
import com.charter.nns.protobuf.filecoordinates.GeneratedResponseFileCoordinates.simple;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Type;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorRequest;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Pattern;
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
            // If type_name is set, [fieldDescriptorProto.type] need not be set.  If both [type] and type_name
            // are set, [type] must be one of TYPE_ENUM, TYPE_MESSAGE or TYPE_GROUP.
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
                .flatMap(TypeUtils::allNestedCoordinates)
                .collect(Collectors.toMap(x -> "." + protoFullTypeName(x), ClassLike::new));
        return new TypeReference(objects);
    }

    static Stream<GeneratedResponseFileCoordinates> allNestedCoordinates(FileDescriptorProto root) {
        return StreamUtil.<GeneratedResponseFileCoordinates>concat(
                root.getMessageTypeList()
                        .stream()
                        .map(descriptorProto -> new simple(root, descriptorProto, null))
                        .flatMap(TypeUtils::allNestedCoordinates),
                root.getEnumTypeList()
                        .stream()
                        .map(enumDescriptorProto ->
                                HACK_coordinatesForEnumDescriptor(root, enumDescriptorProto, null))
        );
    }

    static Stream<GeneratedResponseFileCoordinates> allNestedCoordinates(GeneratedResponseFileCoordinates parent) {
        return StreamUtil.<GeneratedResponseFileCoordinates>concat(
                parent,
                parent.descriptorProto()
                        .getNestedTypeList()
                        .stream()
                        .map(descriptorProto -> new simple(parent.fileDescriptorProto(), descriptorProto, parent))
                        .flatMap(TypeUtils::allNestedCoordinates),
                parent.descriptorProto()
                        .getEnumTypeList()
                        .stream()
                        .map(enumDescriptorProto ->
                                HACK_coordinatesForEnumDescriptor(parent.fileDescriptorProto(), enumDescriptorProto, parent)));
    }

    // TODO: unwise shim, to allow protoTypeName to work
    private static simple HACK_coordinatesForEnumDescriptor(FileDescriptorProto fileDescriptorProto, DescriptorProtos.EnumDescriptorProto enumDescriptorProto, GeneratedResponseFileCoordinates parent) {
        return new simple(
                fileDescriptorProto,
                DescriptorProto.newBuilder()
                        .setName(enumDescriptorProto.getName())
                        .build(),
                parent);
    }

    public static String PACKAGE_SEPERATOR = ".";

    public static void appendPackageSeperatorIfNecessary(StringBuilder out) {
        // append the package separator if the buffer is non-empty, and doesn't end in a dot.
        if (out.length() > 0 && out.charAt(out.length() - 1) != '.') {
            out.append(PACKAGE_SEPERATOR);
        }
    }


    public static StringBuilder protoFullTypeName(GeneratedResponseFileCoordinates coordinates) {
        StringBuilder out;
        GeneratedResponseFileCoordinates parent = coordinates.parent();
        if (parent == null) {
            out = new StringBuilder(coordinates.fileDescriptorProto().getPackage());
        } else {
            out = protoFullTypeName(parent);
        }

        appendPackageSeperatorIfNecessary(out);
        out.append(coordinates.descriptorProto().getName());
        return out;
    }

    public static String javaFullClassName(GeneratedResponseFileCoordinates messageContext) {
        StringBuilder out = javaFullClassNamePrefixBuilder(messageContext);
        appendPackageSeperatorIfNecessary(out);
        out.append(javaClassName(messageContext));

        return out.toString();
    }

    // everything but the final class name - necessary for CustomMixin work
    public static StringBuilder javaFullClassNamePrefixBuilder(GeneratedResponseFileCoordinates fileCoordinates) {
        StringBuilder out = new StringBuilder(javaPackage(fileCoordinates));
        appendPackageSeperatorIfNecessary(out);
        FileDescriptorProto fileDescriptorProto = fileCoordinates.fileDescriptorProto();
        if (!fileDescriptorProto.getOptions().getJavaMultipleFiles()) {
            out.append(javaOuterClassBaseName(fileDescriptorProto));
            appendPackageSeperatorIfNecessary(out);
        }
        appendNestedClassParents(out, fileCoordinates.parent());
        return out;
    }

    // recursive parent finding
    private static void appendNestedClassParents(StringBuilder out, GeneratedResponseFileCoordinates fileCoordinates) {
        if (fileCoordinates == null) {
            return; // do no work
        }
        appendNestedClassParents(out, fileCoordinates.parent());

        appendPackageSeperatorIfNecessary(out);
        out.append(javaClassName(fileCoordinates.descriptorProto()));
    }

    // Warning: javaPackage + javaClass != javaFullClassName (nested classes)
    public static String javaPackage(GeneratedResponseFileCoordinates messageContext) {
        return javaPackage(messageContext.fileDescriptorProto());
    }

    // Warning: javaPackage + javaClass != javaFullClassName (nested classes)
    public static String javaPackage(FileDescriptorProto fileDescriptorProto) {
        DescriptorProtos.FileOptions options = fileDescriptorProto.getOptions();

        if (options.hasJavaPackage()) {
            return options.getJavaPackage();
        } else if (fileDescriptorProto.hasPackage()) {
            return fileDescriptorProto.getPackage();
        } else {
            return "";
        }
    }

    // Warning: javaPackage + javaClass != javaFullClassName (nested classes)
    public static String javaClassName(GeneratedResponseFileCoordinates fileCoordinates) {
        return javaClassName(fileCoordinates.descriptorProto());
    }

    // Warning: javaPackage + javaClass != javaFullClassName (nested classes)
    public static String javaClassName(DescriptorProto descriptorProto) {
        return descriptorProto.getName();
    }

    private static Pattern TRAILING_PROTO_SUFFIX = Pattern.compile("\\.proto$");
    private static String OUTER_CLASS_SUFFIX = "OuterClass";

    public static String javaOuterClassBaseName(FileDescriptorProto fileDescriptorProto) {
        String guess = CodeGeneratorUtils.CamelCase(CodeGeneratorUtils.deleteMatchesOfPattern(TRAILING_PROTO_SUFFIX, fileDescriptorProto.getName()));

        // minor concern: This might be inefficient.
        // If our program is slow, we should count executions
        if (fileDescriptorProto.getMessageTypeList()
                .stream()
                .map(TypeUtils::javaClassName)
                .noneMatch(guess::equals)) {
            return guess;
        } else {
            return guess + OUTER_CLASS_SUFFIX;
        }
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
            return boxingType.isPrimitive(); // Some boxing types are not primitive
        }

        @Override
        public DescriptorProto descriptorProto() {
            return null; // Boxing types are not for messages, and do not have message descriptors
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
                default -> null;// TODO: log a similar error to isPrimitive
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

        @Nullable
        DescriptorProto descriptorProto();

        default DescriptorProto descriptorProtoDefault() {
            DescriptorProto descriptorProto = descriptorProto();
            if (descriptorProto == null) {
                return DescriptorProto.getDefaultInstance();
            } else {
                return descriptorProto;
            }
        }

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

    static record ClassLike(String javaName,
                            String proto_type_name,
                            DescriptorProto descriptorProto) implements TypeNames {
        public ClassLike(GeneratedResponseFileCoordinates protoIdentifier) {
            this(javaFullClassName(protoIdentifier), protoFullTypeName(protoIdentifier).toString(), protoIdentifier.descriptorProto());
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
