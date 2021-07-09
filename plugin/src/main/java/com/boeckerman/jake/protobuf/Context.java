package com.boeckerman.jake.protobuf;

import com.boeckerman.jake.protobuf.Extensions.JavaFieldExtension;
import com.boeckerman.jake.protobuf.Extensions.JavaGlobalOptions;
import com.boeckerman.jake.protobuf.Extensions.JavaMessageExtensions;
import com.boeckerman.jake.protobuf.filecoordinates.GeneratedResponseFileCoordinates;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.EnumDescriptorProto;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorRequest;

public class Context {

    // CLI Parameters:
    public static final String ENABLE_EVERYWHERE = "ENABLE_EVERYWHERE"; // don't require opt-in
    public static final String DEBUG = "DEBUG"; // debug output
    public static final String DEBUG_VERBOSE = "DEBUG_VERBOSE"; // verbose debug output

    static record ExecutionContext(TypeUtils.TypeReference typeNames) {
        public ExecutionContext(CodeGeneratorRequest request) {
            this(TypeUtils.generateLookupTableFor(request));
        }
    }

    static record RootContext(CodeGeneratorRequest request, ExecutionContext executionContext) {
        public RootContext(CodeGeneratorRequest request) {
            this(request, new ExecutionContext(request));
        }

        FileContext withFile(FileDescriptorProto fileDescriptorProto) {
            return new FileContext(this, fileDescriptorProto);
        }
    }

    static JavaGlobalOptions javaExtensionOptionsFor(CodeGeneratorRequest request, FileDescriptorProto fileDescriptorProto) {
        boolean run_everywhere = request.getParameter().contains(ENABLE_EVERYWHERE);
        JavaGlobalOptions extension = fileDescriptorProto.getOptions().getExtension(Extensions.javaHelperGlobals);
        if (run_everywhere) return extension.toBuilder().setEnabled(true).build();
        return extension;
    }

    static record FileContext(CodeGeneratorRequest request,
                              ExecutionContext executionContext,
                              FileDescriptorProto fileDescriptorProto,
                              JavaGlobalOptions javaGlobalOptions) {
        public FileContext(RootContext rootContext, FileDescriptorProto fileDescriptorProto) {
            this(rootContext.request, rootContext.executionContext,
                    fileDescriptorProto, javaExtensionOptionsFor(rootContext.request, fileDescriptorProto));
        }

        MessageContext withMessage(DescriptorProto descriptorProto) {
            return new MessageContext(this, descriptorProto);
        }

        EnumContext withEnum(EnumDescriptorProto enumDescriptorProto) {
            return new EnumContext(this, enumDescriptorProto);
        }
    }

    static JavaMessageExtensions enhancedExtensionOptions(JavaGlobalOptions javaGlobalOptions,
                                                          DescriptorProto descriptorProto) {

        JavaMessageExtensions.Builder builder = merge(javaGlobalOptions.getGlobals(), descriptorProto);

        // allow for a friendly, top-level enabled flag.
        if (!builder.hasEnabled()) {
            builder.setEnabled(javaGlobalOptions.getEnabled());
        }
        return builder.build();
    }

    private static JavaMessageExtensions.Builder merge(JavaMessageExtensions defaults, DescriptorProto descriptorProto) {
        return defaults
                .toBuilder()
                .mergeFrom(descriptorProto
                        .getOptions()
                        .getExtension(Extensions.javaHelperMessage));
    }

    static record MessageContext(CodeGeneratorRequest request,
                                 ExecutionContext executionContext,
                                 FileDescriptorProto fileDescriptorProto,
                                 MessageContext parent,
                                 DescriptorProto descriptorProto,
                                 JavaMessageExtensions javaMessageExtensions)
            implements GeneratedResponseFileCoordinates {
        public MessageContext(FileContext fileContext, DescriptorProto descriptorProto) {
            this(fileContext.request, fileContext.executionContext, fileContext.fileDescriptorProto,
                    null, descriptorProto, enhancedExtensionOptions(fileContext.javaGlobalOptions, descriptorProto));
        }

        public MessageContext(MessageContext parent, DescriptorProto descriptorProto) {
            this(parent.request, parent.executionContext, parent.fileDescriptorProto,
                    parent, descriptorProto, merge(parent.javaMessageExtensions, descriptorProto).build());
        }

        FieldContext withFieldDescriptor(FieldDescriptorProto fieldDescriptorProto) {
            return new FieldContext(this, fieldDescriptorProto);
        }

        @Override
        public MessageContext childFor(DescriptorProto descriptorProto) {
            return new MessageContext(this, descriptorProto);
        }

        @Override
        public EnumContext childFor(EnumDescriptorProto enumDescriptorProto) {
            return new EnumContext(this, enumDescriptorProto);
        }
    }

    static record EnumContext(CodeGeneratorRequest request,
                              ExecutionContext executionContext,
                              FileDescriptorProto fileDescriptorProto,
                              GeneratedResponseFileCoordinates parent,
                              EnumDescriptorProto enumDescriptorProto)
            implements GeneratedResponseFileCoordinates {
        public EnumContext(MessageContext parent, EnumDescriptorProto enumDescriptorProto) {
            this(parent.request, parent.executionContext, parent.fileDescriptorProto, parent,
                    enumDescriptorProto);
        }

        public EnumContext(FileContext parent, EnumDescriptorProto enumDescriptorProto) {
            this(parent.request, parent.executionContext, parent.fileDescriptorProto, null,
                    enumDescriptorProto);
        }

        @Override
        public DescriptorProto descriptorProto() {
            // we're not really a descriptor, but we can act like one
            return DescriptorProto.newBuilder().setName(enumDescriptorProto.getName()).build();
        }
    }

    static JavaFieldExtension enhancedFieldExtensions(JavaMessageExtensions javaExtensionOptions, FieldDescriptorProto fieldDescriptorProto) {
        JavaFieldExtension javaFieldExtension = fieldDescriptorProto.getOptions().getExtension(Extensions.javaHelper);

        JavaFieldExtension.Builder builder = javaExtensionOptions.getOverrides()
                .toBuilder()
                .mergeFrom(javaFieldExtension);
        if (!builder.hasEnabled()) {
            builder.setEnabled(javaExtensionOptions.getEnabled());
        }
        return builder
                .build();
    }

    static record FieldContext(CodeGeneratorRequest request,
                               ExecutionContext executionContext,
                               FileDescriptorProto fileDescriptorProto,
                               DescriptorProto descriptorProto,
                               GeneratedResponseFileCoordinates parent,
                               FieldDescriptorProto fieldDescriptorProto,
                               JavaFieldExtension fieldExtension)
            implements GeneratedResponseFileCoordinates {
        public FieldContext(MessageContext parent, FieldDescriptorProto fieldDescriptorProto) {
            this(parent.request, parent.executionContext, parent.fileDescriptorProto, parent.descriptorProto,
                    parent, fieldDescriptorProto, enhancedFieldExtensions(parent.javaMessageExtensions, fieldDescriptorProto));
        }
    }
}
