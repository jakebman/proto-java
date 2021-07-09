package com.boeckerman.jake.protobuf;

import com.boeckerman.jake.protobuf.filecoordinates.GeneratedResponseFileCoordinates;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.File;

import java.util.stream.Stream;

public class AliasFields implements FieldHandler {


    private final Context.FieldContext fieldContext;

    public AliasFields(Context.FieldContext fieldContext) {

        this.fieldContext = fieldContext;
    }

    @Override
    public GeneratedResponseFileCoordinates context() {
        return fieldContext;
    }

    @Override
    public Stream<File> get() {
        // TODO: implement me
        return Stream.empty();
//        return StreamUtil.<File>concat(
//                options.getAliasesList().stream().flatMap(this::alias),
//                options.getGettersList().stream().map(this::aliasGetter),
//                options.getSettersList().stream().map(this::aliasSetter)
//        );
    }

    private Stream<File> alias(String s) {
        Stream.Builder<File> builder = Stream.builder();
        builder.add(aliasGetter("get" + s));
        builder.add(aliasSetter("set" + s));
        // TODO: getter "is" for booleans
        return builder.build();
    }

    private File aliasSetter(String s) {
        return null;
    }

    private File aliasGetter(String s) {
        return mixinContext("""
                <type> get<alias>() {
                    return get<originaName>();
                }
                """);
    }
}
