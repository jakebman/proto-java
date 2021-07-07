package com.boeckerman.jake.protobuf.filecoordinates;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.DescriptorProtos.FileOptions;

public class DummyCoordinateHelper {

    public static final Coordinates mixin_multiple_files = new Coordinates(new Shim(DescriptorProtos.FileDescriptorProto.newBuilder()
            .setOptions(FileOptions.newBuilder()
                    .setJavaMultipleFiles(true).build())
            .build(), DescriptorProtos.DescriptorProto.newBuilder()
            .build()),
            InsertionPoint.custom_mixin_interface_scope);

    static record Shim(DescriptorProtos.FileDescriptorProto fileDescriptorProto,
                       DescriptorProtos.DescriptorProto descriptorProto) implements GeneratedResponseFileCoordinates {
    }
}
