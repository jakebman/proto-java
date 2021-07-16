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
            return null; // it's likely that methodInvoke should just be a static method. But we'll fix that *last* (we might delete the entire system, so..)
        }

        @Override
        public Stream<PluginProtos.CodeGeneratorResponse.File> get() {
            return null;
        }
    };
    @Test
    public void methodInvoke() {
        // TODOish: there's no space after the comma, despite what the example comment says. Probably not worth fixing
        // flyBar(String one, String two)
        Assert.assertEquals("flyBar(String one,String two)", UNDER_TEST.methodInvoke("fly", "Bar", "String one", "String two").toString());
        // setBar(one, two)
        Assert.assertEquals("setBar(one,two)", UNDER_TEST.methodInvoke("set", "Bar", "one", "two").toString());
        // further ex's
        Assert.assertEquals("fooBarr(a,true,3,33L)", UNDER_TEST.methodInvoke("foo", "Barr", "a", "true", "3", "33L").toString());
        Assert.assertEquals("setComplicatedFieldName(String a,Boolean b,int c,Stream<Stream<File>> complicated arg)", UNDER_TEST.methodInvoke("set", "ComplicatedFieldName", "String a", "Boolean b", "int c", "Stream<Stream<File>> complicated arg").toString());
    }
}