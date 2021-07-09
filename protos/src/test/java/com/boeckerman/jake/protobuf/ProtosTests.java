package com.boeckerman.jake.protobuf;

import com.boeckerman.jake.UsabilityImprovedMessage;
import com.google.protobuf.ByteString;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Unit test for simple App.
 */
public class ProtosTests {
    @Test
    public void theDefaultInstance() {
        Person p = Person.getDefaultInstance();
        assertNotNull(p);
        assertEquals("", p.getName());
    }

    @Test
    public void personIsUsability() {
        Person p = Person.getDefaultInstance();
        assertTrue(p instanceof UsabilityImprovedMessage);
    }

    @Test
    public void serializationChange() throws IOException {
        Person p = Person.getDefaultInstance();
        System.out.println(p.toByteArray().length);
        System.out.println(p.toGzippedByteArray().length);
        byte[] foo = "floccinaucinihilipilification".getBytes();
        ByteString.Output output = ByteString.newOutput(5000);
        for (int i = 0; i < 200; i++) {
            output.write(foo);
        }
        ByteString empty = output.toByteString();
        Person heavy = Person.newBuilder().setArbitraryBytes(empty).build();

        System.out.println(heavy.toByteArray().length);
        System.out.println(heavy.toGzippedByteArray().length);
    }
}
