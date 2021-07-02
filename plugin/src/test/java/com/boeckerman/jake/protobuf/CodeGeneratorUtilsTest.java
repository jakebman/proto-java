package com.boeckerman.jake.protobuf;

import junit.framework.TestCase;

import static com.boeckerman.jake.protobuf.CodeGeneratorUtils.CamelCase;
public class CodeGeneratorUtilsTest extends TestCase {

    public void testCamelCase() {
        assertEquals("", CamelCase(""));
        assertEquals("SnakeCase", CamelCase("snake_case"));

        // If your field name contains a number, the number should appear after
        // the letter instead of after the underscore. e.g., use song_name1 instead of song_name_1
        // https://developers.google.com/protocol-buffers/docs/style
        // But! We'll test both anyway.
        assertEquals("SongName1", CamelCase("song_name1"));
        assertEquals("SongName1", CamelCase("song_name_1"));
        assertEquals("MyProto", CamelCase("my_proto"));
        assertEquals("Embedded3Number", CamelCase("embedded3number")); // prove ZERO_WIDTH_BETWEEN_DIGIT_AND_LETTER is needed

        // Filenames also get this treatment, and they can be wilder
        assertEquals("HyphenatedThing", CamelCase("hyphenated-thing"));
        assertEquals("TheWorStFileName", CamelCase("the `~worSt file !@#$%^&()-_+=name{}[];'.,"));

        /*
        Reference: https://developers.google.com/protocol-buffers/docs/reference/proto3-spec
        Cases not to test:
         * Starts with a number (not a valid identifier)
         * ... or anything that's not a letter for that matter
         * Unicode - letters are Latin alphabet A-Z only.
        Uncertain answers:
         * the_pRoto_file => ThePRotoFile?
         * mucking with `fullIdent`?
         */
    }
}