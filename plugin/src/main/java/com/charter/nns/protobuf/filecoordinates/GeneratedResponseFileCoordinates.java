package com.charter.nns.protobuf.filecoordinates;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.File;

import javax.annotation.Nullable;

// so we can get the file builder for any sufficiently-deep context
public interface GeneratedResponseFileCoordinates {
    // the two record fields that insertion points need to uniquely identify a file edit point
    FileDescriptorProto fileDescriptorProto();

    DescriptorProto descriptorProto();

    @Nullable
    GeneratedResponseFileCoordinates parent();

    default GeneratedResponseFileCoordinates childFor(DescriptorProto child) {
        throw new UnsupportedOperationException("can't nest a message in " + this.getClass());
    }

    default GeneratedResponseFileCoordinates childFor(DescriptorProtos.EnumDescriptorProto child) {
        throw new UnsupportedOperationException("can't nest an enum in " + this.getClass());
    }

    record simple(FileDescriptorProto fileDescriptorProto,
                  DescriptorProto descriptorProto,
                  GeneratedResponseFileCoordinates parent) implements GeneratedResponseFileCoordinates {

    }

    default File.Builder fileBuilderFor(InsertionPoint insertionPoint) {
        return new Coordinates(this, insertionPoint).fileBuilder();
    }
}
