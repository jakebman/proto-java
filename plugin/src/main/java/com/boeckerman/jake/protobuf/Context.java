package com.boeckerman.jake.protobuf;

import com.boeckerman.jake.protobuf.Extensions.JavaFieldExtension;
import com.boeckerman.jake.protobuf.Extensions.JavaGlobalOptions;
import com.boeckerman.jake.protobuf.Extensions.JavaMessageExtensions;
import com.boeckerman.jake.protobuf.filecoordinates.GeneratedResponseFileCoordinates;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorRequest;

public class Context {

    // CLI Parameters:
    public static final String ENABLE_EVERYWHERE = "ENABLE_EVERYWHERE"; // don't require opt-in
    public static final String DEBUG = "DEBUG"; // debug output
    public static final String DEBUG_VERBOSE = "DEBUG_VERBOSE"; // verbose debug output

    static record RootContext(CodeGeneratorRequest request) {
        FileContext withFile(FileDescriptorProto fileDescriptorProto) {
            return new FileContext(request, fileDescriptorProto, javaExtensionOptionsFor(request, fileDescriptorProto));
        }
    }

    static JavaGlobalOptions javaExtensionOptionsFor(CodeGeneratorRequest request, FileDescriptorProto fileDescriptorProto) {
        boolean run_everywhere = request.getParameter().contains(ENABLE_EVERYWHERE);
        JavaGlobalOptions extension = fileDescriptorProto.getOptions().getExtension(Extensions.javaHelperGlobals);
        if (run_everywhere) return extension.toBuilder().setEnabled(true).build();
        return extension;
    }

    static record FileContext(CodeGeneratorRequest request,
                              FileDescriptorProto fileDescriptorProto,
                              JavaGlobalOptions javaGlobalOptions) {

        MessageContext withMessage(DescriptorProto descriptorProto) {
            return new MessageContext(request, fileDescriptorProto,
                    descriptorProto,
                    enhancedExtensionOptions(javaGlobalOptions, descriptorProto));
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
                                 FileDescriptorProto fileDescriptorProto,
                                 DescriptorProto descriptorProto,
                                 JavaMessageExtensions javaMessageExtensions)
            implements GeneratedResponseFileCoordinates {

        FieldContext withFieldDescriptor(FieldDescriptorProto fieldDescriptorProto) {
            return new FieldContext(request, fileDescriptorProto, descriptorProto,
                    fieldDescriptorProto,
                    enhancedFieldExtensions(javaMessageExtensions, fieldDescriptorProto));
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
                               FileDescriptorProto fileDescriptorProto,
                               DescriptorProto descriptorProto,
                               FieldDescriptorProto fieldDescriptorProto,
                               JavaFieldExtension fieldExtension)
            implements GeneratedResponseFileCoordinates {

    }
}
