package com.boeckerman.jake.protobuf;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit test for simple App.
 */
public class ProtosTests
{
    @Test
    public void theDefaultInstance()
    {
        Person p = Person.getDefaultInstance();
        assertNotNull(p);
        assertEquals("", p.getName());
    }
}
