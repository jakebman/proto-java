package com.boeckerman.jake.protobuf.filecoordinates;

import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.File;

import java.util.concurrent.Semaphore;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;


public class CustomMixinFile {

    public static final String MIXIN_SUFFIX = "_Mixin";

    // Returns a stateful, synchronized BiConsumer good for a single Stream.mapMulti call
    // This is safe on any stream (including parallel, unordered), HOWEVER, it is also good
    // for the custom mixin file generated *before* any File that references it, and the
    // definition of "before" is lacking in an unordered stream.
    public static BiConsumer<File, Consumer<File>> alsoEmitMixinFileWhenNeeded(GeneratedResponseFileCoordinates messageContext) {
        Semaphore x = new Semaphore(1);

        Coordinates coordinates = new Coordinates(messageContext, InsertionPoint.custom_mixin_interface_scope);
        return (file, yield) -> alsoEmitMixinFileWhenNeeded(coordinates, file, yield, x::tryAcquire);
    }

    private static void alsoEmitMixinFileWhenNeeded(Coordinates coordinates, File file, Consumer<File> yield, Supplier<Boolean> succeedsExactlyOnce) {
        if (InsertionPoint.custom_mixin_interface_scope.recognizes(file) && succeedsExactlyOnce.get()) {
            yield.accept(generateMixin(coordinates));
            yield.accept(generateMixinImport(coordinates));
        }
        yield.accept(file);
    }

    private static File generateMixinImport(Coordinates coordinates) {
        return coordinates
                .withInsertionPoint(InsertionPoint.interface_extends)
                .fileBuilder()
                .setContent(coordinates
                        .javaFullClassNameBuilder()
                        .append(",")
                        .toString())
                .build();
    }

    public static File generateMixin(Coordinates coordinates) {
        return coordinates.fileBuilder()
                .clearInsertionPoint() // this is where we *create* that insertion point!
                .setContent("""
                        package %s;
                        public interface %s {
                            // @@protoc_insertion_point(%s)
                        }
                        """.formatted(
                        coordinates.javaPackage(),
                        coordinates.javaClassName(),
                        coordinates.insertionPointFor()
                ))
                .build();
    }
}
