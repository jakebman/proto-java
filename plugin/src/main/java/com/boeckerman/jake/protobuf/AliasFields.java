package com.boeckerman.jake.protobuf;

import com.boeckerman.jake.protobuf.Extensions.JavaFieldExtension.AliasOptions;
import com.boeckerman.jake.protobuf.Extensions.JavaFieldExtension.BooleanOptions;
import com.boeckerman.jake.protobuf.NameVariants.FieldNames;
import com.boeckerman.jake.protobuf.TypeUtils.TypeNames;
import com.boeckerman.jake.protobuf.filecoordinates.GeneratedResponseFileCoordinates;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.File;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AliasFields implements FieldHandler, GetterSetterHelper {


    private final Context.FieldContext fieldContext;
    private final BooleanOptions booleanOptions;
    private final AliasOptions options;
    private final TypeNames typeNames;
    private final FieldNames nameVariants;

    public AliasFields(Context.FieldContext fieldContext) {
        this.fieldContext = fieldContext;
        this.options = fieldContext.fieldExtension().getAlias();
        this.booleanOptions = fieldContext.fieldExtension().getBoolean();
        this.typeNames = fieldContext.executionContext().typeNames().lookup(fieldContext.fieldDescriptorProto());

        this.nameVariants = new FieldNames(fieldContext);
    }

    @Override
    public GeneratedResponseFileCoordinates context() {
        return fieldContext;
    }

    @Override
    public Stream<File> get() {
        Collection<String> aliases = options.getAliasesList().stream().map(StringUtils::capitalize).collect(Collectors.toList());

        Stream<String> getters = StreamUtil.concat(
                options.getGettersList().stream(),
                aliases.stream().map(x -> "get" + x),
                aliasesWithIsPrefix(aliases));

        Stream<String> setters = StreamUtil.<String>concat(
                options.getSettersList().stream(),
                aliases.stream().map(x -> "set" + x));

        Stream<String> clearers = StreamUtil.<String>concat(
                options.getClearersList().stream(),
                aliases.stream().map(x -> "clear" + x));

        return StreamUtil.<File>concat(
                generateGetters(getters),
                generateSetters(setters),
                generateClearers(clearers)
        );
    }

    private Stream<String> aliasesWithIsPrefix(Collection<String> aliases) {
        if (booleanOptions.getUseIsPrefix()
                && typeNames instanceof TypeUtils.ProtoAndJavaTypeNames boxType
                && boxType.boxingType() == TypeUtils.BoxingType.Boolean) {
            return StreamUtil.concat(
                    "is" + nameVariants.name(),
                    aliases.stream().map(x -> "is" + x));
        }
        return Stream.empty();
    }

    private Stream<File> generateGetters(Stream<String> getters) {
        Stream<File> fileStream = getters.map(this::aliasGetter);
        if (nameVariants.hasNullable()) {
            // a getter declaration already appears in the Mixin
            // todo: need to check this works always.
            return fileStream;
        }
        List<File> generated = fileStream.collect(Collectors.toList());
        if (generated.isEmpty()) {
            return Stream.empty();
        } else {
            return StreamUtil.concat(
                    getter(), // generate the getter that these all alias
                    generated.stream()
            );
        }
    }

    private File aliasGetter(String method) {
        return mixinContext("""
                default %s %s() // alias getter
                {
                    return %s;
                }
                """.formatted(typeNames.boxed(),
                method,
                methodInvoke("get", nameVariants().protoGeneratedName())));
    }

    private Stream<File> generateSetters(Stream<String> setters) {
        return setters.map(this::aliasSetter);
    }


    private File aliasSetter(String method) {
        return builderContext("""
                Builder %s(%s value) // alias setter
                {
                    return %s;
                }
                """.formatted(method,
                typeNames.boxed(),
                methodInvoke("set", nameVariants().protoGeneratedName(), "value")));
    }

    private Stream<File> generateClearers(Stream<String> setters) {
        return setters.map(this::aliasClearer);
    }

    private File aliasClearer(String method) {
        return builderContext("""
                Builder %s(%s value) // alias setter
                {
                    return %s;
                }
                """.formatted(method,
                typeNames.boxed(),
                methodInvoke("clear", nameVariants().protoGeneratedName(), "value")));
    }


    @Override
    public FieldNames nameVariants() {
        return nameVariants;
    }

    @Override
    public TypeNames typeNames() {
        return typeNames;
    }
}
