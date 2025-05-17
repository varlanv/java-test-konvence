package com.varlanv.testkonvence.gradle.plugin;

import java.util.*;
import java.util.function.Function;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

final class ImmutableList<T> implements Iterable<@NonNull T> {

    private final List<@NonNull T> value;

    private ImmutableList(List<@NonNull T> value) {
        this.value = value;
    }

    public static <T> ImmutableList<T> copyOf(Collection<@Nullable T> collection) {
        var result = new ArrayList<@NonNull T>(collection.size());
        for (var t : collection) {
            if (t == null) {
                throw new IllegalArgumentException();
            }
            result.add(t);
        }
        return new ImmutableList<>(result);
    }

    public static <T> ImmutableList<T> copyOfWithoutNulls(Collection<@Nullable T> collection) {
        var result = new ArrayList<@NonNull T>(collection.size());
        for (var t : collection) {
            if (t != null) {
                result.add(t);
            }
        }
        return new ImmutableList<>(result);
    }

    public static <T> ImmutableList<T> empty() {
        List<@NonNull T> list = Collections.emptyList();
        return new ImmutableList<T>(list);
    }

    public List<@NonNull T> value() {
        return Collections.unmodifiableList(value);
    }

    public <R> ImmutableList<R> mapSkippingNull(Function<T, @Nullable R> mapper) {
        if (value.isEmpty()) {
            return ImmutableList.empty();
        } else {
            var result = new ArrayList<@NonNull R>(value.size());
            for (var t : value) {
                R mapped = mapper.apply(t);
                if (mapped != null) {
                    result.add(mapped);
                }
            }
            return new ImmutableList<>(result);
        }
    }

    @Override
    public String toString() {
        return value.toString();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (!(o instanceof ImmutableList)) return false;
        var that = (ImmutableList<?>) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public @NonNull Iterator<@NonNull T> iterator() {
        return value.iterator();
    }
}
