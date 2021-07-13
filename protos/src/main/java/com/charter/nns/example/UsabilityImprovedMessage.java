package com.charter.nns.example;

import com.google.protobuf.Descriptors;
import com.google.protobuf.Message;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.stream.Stream;
import java.util.zip.GZIPOutputStream;

public interface UsabilityImprovedMessage extends Message {

    default Stream<Descriptors.FieldDescriptor> anAbsurdName() {
        return this.getAllFields().keySet().stream();
    }

    default byte[] toGzippedByteArray() throws IOException {
        ByteArrayOutputStream outputStream = getByteArrayOutputStream();
        return outputStream.toByteArray(); // nb: copies the array out. could be more efficient.
    }

    default ByteArrayOutputStream getByteArrayOutputStream() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (BufferedOutputStream buffer = new BufferedOutputStream(outputStream);
             GZIPOutputStream writeable = new GZIPOutputStream(buffer)) {
            this.writeTo(writeable);
        }
        outputStream.close();
        return outputStream;
    }
}
