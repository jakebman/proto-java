package com.boeckerman.jake.protobuf;

import org.apache.commons.lang3.text.WordUtils;

public class CodeGeneratorUtils {
    static String CamelCase(String name_with_underscores) {
        return WordUtils.capitalizeFully(name_with_underscores, '_');
    }
}
