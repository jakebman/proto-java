package com.boeckerman.jake.protobuf;

import com.boeckerman.jake.protobuf.Extensions.JavaExtensionOptions;
import com.boeckerman.jake.protobuf.Extensions.JavaFieldExtension;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.compiler.PluginProtos;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorRequest;

public class Context {

    // so we can get the file builder for any sufficiently-deep context
    interface GeneratedResponseFileCoordinates {
        // the two record fields that insertion points need to uniquely identify a file edit point
        FileDescriptorProto fileDescriptorProto();

        DescriptorProto descriptorProto();

        default PluginProtos.CodeGeneratorResponse.File.Builder fileBuilderFor(InsertionPoint insertionPoint) {
            return insertionPoint.fileBuilderFor(this);
        }
    }

    static record RootContext(CodeGeneratorRequest request) {
        FileContext withFile(FileDescriptorProto fileDescriptorProto) {
            return new FileContext(request, fileDescriptorProto, javaExtensionOptionsFor(fileDescriptorProto));
        }
    }

    static JavaExtensionOptions javaExtensionOptionsFor(FileDescriptorProto fileDescriptorProto) {
        return fileDescriptorProto.getOptions().getExtension(Extensions.javaHelperGlobals);
    }

    static record FileContext(CodeGeneratorRequest request,
                              FileDescriptorProto fileDescriptorProto,
                              JavaExtensionOptions javaExtensionOptions) {

        MessageContext withMessage(DescriptorProto descriptorProto) {
            return new MessageContext(request, fileDescriptorProto,
                    descriptorProto,
                    enhancedExtensionOptions(javaExtensionOptions, descriptorProto));
        }

    }

    static JavaExtensionOptions enhancedExtensionOptions(JavaExtensionOptions javaExtensionOptions,
                                                         DescriptorProto descriptorProto) {
        return javaExtensionOptions
                .toBuilder()
                .mergeFrom(descriptorProto
                        .getOptions()
                        .getExtension(Extensions.javaHelperMessage))
                .build();
    }

    static record MessageContext(CodeGeneratorRequest request,
                                 FileDescriptorProto fileDescriptorProto,
                                 DescriptorProto descriptorProto,
                                 JavaExtensionOptions javaExtensionOptions)
            implements GeneratedResponseFileCoordinates {

        FieldContext withField(FieldDescriptorProto fieldDescriptorProto) {
            return new FieldContext(request, fileDescriptorProto, descriptorProto,
                    fieldDescriptorProto,
                    enhancedFieldExtensions(javaExtensionOptions, fieldDescriptorProto));
        }
    }


    static JavaFieldExtension enhancedFieldExtensions(JavaExtensionOptions javaExtensionOptions, FieldDescriptorProto fieldDescriptorProto) {
        JavaFieldExtension javaFieldExtension = fieldDescriptorProto.getOptions().getExtension(Extensions.javaHelper);
        JavaFieldExtension.Builder builder = javaFieldExtension.toBuilder();
        builder.mergeNullable(javaExtensionOptions.getNullable());
        builder.mergeList(javaExtensionOptions.getList());
        builder.mergeAlias(javaExtensionOptions.getAlias()); // nb: alias lists are concatenated
        builder.mergeBoolean(javaExtensionOptions.getBoolean());
        if (!builder.hasEnabled()) {
            builder.setEnabled(javaExtensionOptions.getEnabled());
        }

        return builder.build();
    }

    static record FieldContext(CodeGeneratorRequest request,
                               FileDescriptorProto fileDescriptorProto,
                               DescriptorProto descriptorProto,
                               FieldDescriptorProto fieldDescriptorProto,
                               JavaFieldExtension fieldExtension)
            implements GeneratedResponseFileCoordinates {

    }
}
