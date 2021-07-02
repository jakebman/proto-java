package com.boeckerman.jake.protobuf.nested;

import java.util.Iterator;
import java.util.stream.Stream;

public interface NestedStreamingIterable<T> extends Iterable<T> {
    default Stream<NestedStreamingIterable<T>> children() {
        return Stream.empty();
    }

    default Stream<T> stream() {
        return children().flatMap(NestedStreamingIterable::stream);
    }

    @Override
    default Iterator<T> iterator() {
        return stream().iterator();
    }
}
