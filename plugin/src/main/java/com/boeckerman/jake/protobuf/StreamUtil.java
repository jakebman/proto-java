package com.boeckerman.jake.protobuf;

import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class StreamUtil {
    // Useful for ordered streams. Not useful for unordered streams.
    // https://www.techempower.com/blog/2016/10/19/efficient-multiple-stream-concatenation-in-java/

    // provided as a base case, and to allow for source code to just reference StreamUtil.concat
    // in all situations
    public static <T> Stream<T> concat(Stream<T> a, Stream<T> b) {
        return Stream.concat(a, b);
    }

    public static <T> Stream<T> concat(T a, Stream<T> b) {
        return concat(Stream.of(a), b);
    }

    @SafeVarargs
    public static <T> Stream<T> concat(Stream<T>... streams) {
        return Arrays.stream(streams).flatMap(Function.identity());
    }

    @SafeVarargs
    public static <T> Stream<T> concat(Supplier<Stream<T>>... streams) {
        return Arrays.stream(streams).flatMap(Supplier::get);
    }

    @SafeVarargs
    public static <T> Stream<T> concat(T a, Stream<T>... streams) {
        return concat(
                Stream.of(a),
                concat(streams));
    }
//
//    @SafeVarargs
//    public static <T> Stream<T> concat(T a, T b, Stream<T>... streams) {
//        return concat(
//                Stream.of(a, b),
//                concat(streams));
//    }
}
