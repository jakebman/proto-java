package com.boeckerman.jake.protobuf.filecoordinates;

import com.boeckerman.jake.protobuf.filecoordinates.GeneratedResponseFileCoordinates;
import com.boeckerman.jake.protobuf.filecoordinates.InsertionPoint;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.File;

import java.util.concurrent.Semaphore;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import static com.boeckerman.jake.protobuf.filecoordinates.GeneratedResponseFileCoordinates.JAVA_FILENAME_SUFFIX;

public class CustomMixinFile {

    public static final String MIXIN_SUFFIX = "_Mixin";

    // Returns a stateful, synchronized BiConsumer good for a single Stream.mapMulti call
    // This is safe on any stream (including parallel, unordered), HOWEVER, it is also good
    // for the custom mixin file generated *before* any File that references it, and the
    // definition of "before" is lacking in an unordered stream.
    public static BiConsumer<File, Consumer<File>> alsoEmitMixinFileWhenNeeded(GeneratedResponseFileCoordinates messageContext) {
        Semaphore x = new Semaphore(1);
        return (file, yield) -> alsoEmitMixinFileWhenNeeded(messageContext, file, yield, x::tryAcquire);
    }

    private static void alsoEmitMixinFileWhenNeeded(GeneratedResponseFileCoordinates messageContext, File file, Consumer<File> yield, Supplier<Boolean> succeedsExactlyOnce) {
        if (InsertionPoint.custom_mixin_interface_scope.recognizes(file) && succeedsExactlyOnce.get()) {
            yield.accept(generateMixin(messageContext));
            yield.accept(generateMixinImport(messageContext));
        }
        yield.accept(file);
    }

    private static File generateMixinImport(GeneratedResponseFileCoordinates messageContext) {
        return messageContext.fileBuilderFor(InsertionPoint.interface_extends)
                .setContent(mixinFullClassNameFor(messageContext).append(",").toString())
                .build();
    }

    /**
     * See also: {@link GeneratedResponseFileCoordinates#modificationFileAndPath()},
     * which builds a string path
     */
    private static StringBuilder mixinFullClassNameFor(GeneratedResponseFileCoordinates fileIdentifier) {
        StringBuilder sb = new StringBuilder(mixinPackageNameFor(fileIdentifier));
        if (!sb.isEmpty()) {
            sb.append(GeneratedResponseFileCoordinates.PACKAGE_SEPERATOR);
        }
        sb.append(mixinClassNameFor(fileIdentifier));
        return sb;
    }

    private static String mixinClassNameFor(GeneratedResponseFileCoordinates fileIdentifier) {
        return fileIdentifier.modificationFileBaseName() + MIXIN_SUFFIX;
    }

    private static String mixinPackageNameFor(GeneratedResponseFileCoordinates fileIdentifier) {
        return fileIdentifier.javaPackage();
    }

    static Pattern TRAILING_JAVA_SUFFIX = Pattern.compile(Pattern.quote(JAVA_FILENAME_SUFFIX) + "$");

    private static String mixinFileName(GeneratedResponseFileCoordinates fileIdentifier) {
        return TRAILING_JAVA_SUFFIX.matcher(fileIdentifier.modificationFileAndPath()).replaceFirst(MIXIN_SUFFIX + JAVA_FILENAME_SUFFIX);
    }

    public static File generateMixin(GeneratedResponseFileCoordinates fileIdentifier) {
        return File.newBuilder()
                .setName(mixinFileName(fileIdentifier))
                .setContent("""
                        package %s;
                        public interface %s {
                            // @@protoc_insertion_point(%s)
                        }
                        """.formatted(
                        mixinPackageNameFor(fileIdentifier),
                        mixinClassNameFor(fileIdentifier),
                        InsertionPoint.custom_mixin_interface_scope.insertionPointFor(fileIdentifier)
                ))
                .build();
    }
}
