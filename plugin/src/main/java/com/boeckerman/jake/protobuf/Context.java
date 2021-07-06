package com.boeckerman.jake.protobuf;

import com.boeckerman.jake.protobuf.Extensions.JavaExtensionOptions;
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
            return new FileContext(request, fileDescriptorProto);
        }
    }

    static record FileContext(CodeGeneratorRequest request,
                              FileDescriptorProto fileDescriptorProto) {

        MessageContext withMessage(DescriptorProto descriptorProto) {
            return new MessageContext(request, fileDescriptorProto, descriptorProto, javaExtensionOptionsFor(descriptorProto));
        }
    }

    static JavaExtensionOptions javaExtensionOptionsFor(DescriptorProto descriptorProto) {
        return descriptorProto.getOptions().getExtension(Extensions.javaHelper);
    }

    static record MessageContext(CodeGeneratorRequest request,
                                 FileDescriptorProto fileDescriptorProto,
                                 DescriptorProto descriptorProto,
                                 JavaExtensionOptions javaExtensionOptions)
            implements GeneratedResponseFileCoordinates {
        FieldContext withField(FieldDescriptorProto fieldDescriptorProto) {
            return new FieldContext(request, fileDescriptorProto, descriptorProto, javaExtensionOptions, fieldDescriptorProto);
        }
    }

    static record FieldContext(CodeGeneratorRequest request,
                               FileDescriptorProto fileDescriptorProto,
                               DescriptorProto descriptorProto,
                               JavaExtensionOptions javaExtensionOptions,
                               FieldDescriptorProto fieldDescriptorProto)
            implements GeneratedResponseFileCoordinates {

    }
}
