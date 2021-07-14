package com.charter.nns.protobuf.filecoordinates;

import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.File;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import static org.junit.Assert.assertThat;

public class CustomMixinFileTest {

    @Test
    public void generateMixin() {
        File file = CustomMixinFile.generateMixin(DummyCoordinateHelper.mixin_multiple_files);
        assertThat(file.getName(), CoreMatchers.containsString("Mixin"));
    }
}