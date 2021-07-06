package com.boeckerman.jake.protobuf;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.compiler.PluginProtos;

public class Context {

    // so we can get the file builder for any sufficiently-deep context
    interface GeneratedResponseFileCoordinates {
        // the two record fields that insertion points need to uniquely identify a file edit point
        DescriptorProtos.FileDescriptorProto fileDescriptorProto();
        DescriptorProtos.DescriptorProto descriptorProto();

        default PluginProtos.CodeGeneratorResponse.File.Builder fileBuilderFor(InsertionPoint insertionPoint) {
            return insertionPoint.fileBuilderFor(this);
        }
    }

    static record RootContext(PluginProtos.CodeGeneratorRequest request) {
        FileContext withFile(DescriptorProtos.FileDescriptorProto fileDescriptorProto) {
            return new FileContext(request, fileDescriptorProto);
        }
    }

    static record FileContext(PluginProtos.CodeGeneratorRequest request,
                              DescriptorProtos.FileDescriptorProto fileDescriptorProto) {

        MessageContext withMessage(DescriptorProtos.DescriptorProto descriptorProto) {
            return new MessageContext(request, fileDescriptorProto, descriptorProto, javaExtensionOptionsFor(descriptorProto));
        }
    }

    static Extensions.JavaExtensionOptions javaExtensionOptionsFor(DescriptorProtos.DescriptorProto descriptorProto) {
        return descriptorProto.getOptions().getExtension(Extensions.javaHelper);
    }

    static record MessageContext(PluginProtos.CodeGeneratorRequest request,
                                 DescriptorProtos.FileDescriptorProto fileDescriptorProto,
                                 DescriptorProtos.DescriptorProto descriptorProto,
                                 Extensions.JavaExtensionOptions javaExtensionOptions)
            implements GeneratedResponseFileCoordinates {
        FieldContext withField(DescriptorProtos.FieldDescriptorProto fieldDescriptorProto) {
            return new FieldContext(request, fileDescriptorProto, descriptorProto, javaExtensionOptions, fieldDescriptorProto);
        }
    }

    static record FieldContext(PluginProtos.CodeGeneratorRequest request,
                               DescriptorProtos.FileDescriptorProto fileDescriptorProto,
                               DescriptorProtos.DescriptorProto descriptorProto,
                               Extensions.JavaExtensionOptions javaExtensionOptions,
                               DescriptorProtos.FieldDescriptorProto fieldDescriptorProto)
            implements GeneratedResponseFileCoordinates {

    }
}
