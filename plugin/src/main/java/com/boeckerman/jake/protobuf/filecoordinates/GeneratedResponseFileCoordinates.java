package com.boeckerman.jake.protobuf.filecoordinates;

import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.File;

// so we can get the file builder for any sufficiently-deep context
public interface GeneratedResponseFileCoordinates {
    // the two record fields that insertion points need to uniquely identify a file edit point
    FileDescriptorProto fileDescriptorProto();

    DescriptorProto descriptorProto();

    default File.Builder fileBuilderFor(InsertionPoint insertionPoint) {
        return new Coordinates(this, insertionPoint).fileBuilder();
    }
}
