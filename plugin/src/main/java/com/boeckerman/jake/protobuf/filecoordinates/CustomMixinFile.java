package com.boeckerman.jake.protobuf.filecoordinates;

import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.File;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.Semaphore;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.boeckerman.jake.protobuf.filecoordinates.InsertionPoint.outer_class_scope;

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
                .setContent(coordinates.javaClassName() + ",")
                .build();
    }

    public static File generateMixin(Coordinates coordinates) {
        File.Builder builder = coordinates.fileBuilder();
        boolean isGeneratingAFullMixinFile = builder.getName().endsWith(MIXIN_SUFFIX + ".java");
        if (isGeneratingAFullMixinFile) {
            // this is where we *create* the insertion points for this file!
            // TODO: nothing keeps someone from setting java_outer_class=asdf_Mixin
            builder.clearInsertionPoint();
        } else {
            // If we're in an outer class
            builder.setInsertionPoint(coordinates.withInsertionPoint(outer_class_scope).insertionPointFor());
        }
        return builder
                .setContent("""
                        %s
                        public interface %s {
                            // @@protoc_insertion_point(%s)
                        }
                        """.formatted(
                        (isGeneratingAFullMixinFile ? packageDeclaration(coordinates) : ""),
                        coordinates.javaClassName(),
                        coordinates.insertionPointFor()
                ))
                .build();
    }

    private static String packageDeclaration(Coordinates coordinates) {
        String javaPackage = coordinates.javaPackage();
        if (StringUtils.isNotBlank(javaPackage))
            return "package %s;".formatted(javaPackage);
        return "";
    }
}
