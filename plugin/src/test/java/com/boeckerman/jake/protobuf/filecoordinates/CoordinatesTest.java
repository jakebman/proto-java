package com.boeckerman.jake.protobuf.filecoordinates;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class CoordinatesTest {
    private final Coordinates mixin = DummyCoordinateHelper.mixin_multiple_files;


    @Test
    public void mixinNames() {
        assertThat(mixin.javaClassName(), is("_Mixin"));
        assertThat(mixin.modificationFileBaseName(), is("_Mixin"));
        assertThat(mixin.modificationFileAndPath(), is("_Mixin.java"));
    }

}