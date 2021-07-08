package com.boeckerman.jake.protobuf;

import com.boeckerman.jake.protobuf.Extensions.JavaFieldExtension;
import com.boeckerman.jake.protobuf.Extensions.JavaGlobalOptions;
import com.boeckerman.jake.protobuf.Extensions.JavaMessageExtensions;
import com.boeckerman.jake.protobuf.filecoordinates.GeneratedResponseFileCoordinates;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorRequest;

import java.util.Map;

public class Context {

    // CLI Parameters:
    public static final String ENABLE_EVERYWHERE = "ENABLE_EVERYWHERE"; // don't require opt-in
    public static final String DEBUG = "DEBUG"; // debug output
    public static final String DEBUG_VERBOSE = "DEBUG_VERBOSE"; // verbose debug output

    static record ExecutionContext(Map<String, TypeUtils.JavaTypeNames> typeNames) {
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
                              JavaGlobalOptions javaGlobalOptions
    ) {
        public FileContext(RootContext rootContext, FileDescriptorProto fileDescriptorProto) {
            this(rootContext.request, rootContext.executionContext,
                    fileDescriptorProto, javaExtensionOptionsFor(rootContext.request, fileDescriptorProto));
        }

        MessageContext withMessage(DescriptorProto descriptorProto) {
            return new MessageContext(this, descriptorProto);
        }

    }

    static JavaMessageExtensions enhancedExtensionOptions(JavaGlobalOptions javaGlobalOptions,
                                                          DescriptorProto descriptorProto) {

        JavaMessageExtensions.Builder builder = javaGlobalOptions
                .getGlobals()
                .toBuilder()
                .mergeFrom(descriptorProto
                        .getOptions()
                        .getExtension(Extensions.javaHelperMessage));

        if (!builder.hasEnabled()) {
            builder.setEnabled(javaGlobalOptions.getEnabled());
        }
        return builder.build();
    }

    static record MessageContext(CodeGeneratorRequest request,
                                 ExecutionContext executionContext,
                                 FileDescriptorProto fileDescriptorProto,
                                 DescriptorProto descriptorProto,
                                 JavaMessageExtensions javaMessageExtensions)
            implements GeneratedResponseFileCoordinates {
        public MessageContext(FileContext fileContext, DescriptorProto descriptorProto) {
            this(fileContext.request, fileContext.executionContext, fileContext.fileDescriptorProto,
                    descriptorProto, enhancedExtensionOptions(fileContext.javaGlobalOptions, descriptorProto)
            );
        }

        FieldContext withFieldDescriptor(FieldDescriptorProto fieldDescriptorProto) {
            return new FieldContext(this, fieldDescriptorProto);
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
                               FieldDescriptorProto fieldDescriptorProto,
                               JavaFieldExtension fieldExtension
    )
            implements GeneratedResponseFileCoordinates {
        public FieldContext(MessageContext messageContext, FieldDescriptorProto fieldDescriptorProto) {
            this(messageContext.request, messageContext.executionContext, messageContext.fileDescriptorProto, messageContext.descriptorProto,
                    fieldDescriptorProto, enhancedFieldExtensions(messageContext.javaMessageExtensions, fieldDescriptorProto));
        }
    }
}
