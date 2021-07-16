package com.charter.nns.protobuf;

import com.charter.nns.protobuf.filecoordinates.GeneratedResponseFileCoordinates;
import com.google.protobuf.compiler.PluginProtos;
import org.junit.Assert;
import org.junit.Test;

import java.util.stream.Stream;

import static org.junit.Assert.*;

public class FieldHandlerTest {
    FieldHandler UNDER_TEST = new FieldHandler() {
        @Override
        public GeneratedResponseFileCoordinates context() {
            return null; // it's likely that the methods we're testing here should just be static methods. But we'll fix that *last* (we might delete the entire system, so..)
        }

        @Override
        public Stream<PluginProtos.CodeGeneratorResponse.File> get() {
            return null;
        }
    };

    @Test
    public void  methodDeclarationHeader() {
        // String getFoo()
        Assert.assertEquals("String getFoo()", UNDER_TEST.methodDeclarationHeader("String", "get", "Foo").toString());
        // Object flyBar(String one, String two)
        Assert.assertEquals("Object flyBar(String one,String two)", UNDER_TEST.methodDeclarationHeader("Object", "fly", "Bar", "String one", "String two").toString());
    }
}