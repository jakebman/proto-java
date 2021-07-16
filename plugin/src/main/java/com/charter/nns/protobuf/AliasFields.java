package com.charter.nns.protobuf;

import com.charter.nns.protobuf.Extensions.JavaFieldExtension.AliasOptions;
import com.charter.nns.protobuf.Extensions.JavaFieldExtension.BooleanOptions;
import com.charter.nns.protobuf.NameVariants.FieldNames;
import com.charter.nns.protobuf.TypeUtils.BoxingType;
import com.charter.nns.protobuf.TypeUtils.ProtoAndJavaTypeNames;
import com.charter.nns.protobuf.TypeUtils.TypeNames;
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
    public Context.FieldContext context() {
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
        // TODO: has-aliases

        return StreamUtil.concat(
                generateGetters(getters),
                generateSetters(setters),
                generateClearers(clearers)
        );
    }

    private Stream<String> aliasesWithIsPrefix(Collection<String> aliases) {
        if (isBoolean() && !isList()) {
            // Only boolean fields need this. Lists do not need ths
            return StreamUtil.concat(
                    "is" + nameVariants.name(),
                    aliases.stream().map(x -> "is" + x));
        } else {
            return Stream.empty();
        }
    }

    private boolean isBoolean() {
        return booleanOptions.getUseIsPrefix()
                && typeNames instanceof ProtoAndJavaTypeNames boxType
                && boxType.boxingType() == BoxingType.Boolean;
    }

    private Stream<File> generateGetters(Stream<String> getters) {
        Stream<File> fileStream = getters.map(this::aliasGetter);
        if (nameVariants.hasNullable()) {
            // a getter declaration already appears in the Mixin, from nullable
            // todo: need to check this works always.
            return fileStream;
        }
        if (isList() && fieldContext.fieldExtension().getList().getFriendlyGetter()) {
            // a getter declaration already appears in the mixin, from ListFields
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
                    return get%s();
                }
                """.formatted(typeNames.boxed(),
                method,
                nameVariants().protoGeneratedName()));
    }

    private Stream<File> generateSetters(Stream<String> setters) {
        return setters.map(this::aliasSetter);
    }


    private File aliasSetter(String method) {
        return builderContext("""
                public final Builder %s(%s value) // alias setter
                {
                    return set%s(value);
                }
                """.formatted(method,
                typeNames.boxed(),
                nameVariants().protoGeneratedName()));
    }

    private Stream<File> generateClearers(Stream<String> setters) {
        return setters.map(this::aliasClearer);
    }

    private File aliasClearer(String method) {
        return builderContext("""
                public final Builder %s() // alias clear-er
                {
                    return clear%s();
                }
                """.formatted(method,
                nameVariants().protoGeneratedName()));
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
