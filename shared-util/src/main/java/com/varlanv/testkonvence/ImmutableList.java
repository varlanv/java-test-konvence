package com.varlanv.testkonvence;

import java.util.*;
import java.util.function.Function;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class ImmutableList<T> implements Iterable<@NonNull T> {

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

    public boolean isEmpty() {
        return value.isEmpty();
    }

    public int size() {
        return value.size();
    }

    public <R> ImmutableList<R> mapOptional(Function<T, Optional<@NonNull R>> mapper) {
        if (value.isEmpty()) {
            return empty();
        } else {
            var result = new ArrayList<@NonNull R>(value.size());
            for (var t : value) {
                Optional<@NonNull R> mapped = mapper.apply(t);
                mapped.ifPresent(result::add);
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
        if (!(o instanceof ImmutableList)) {
            return false;
        }
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
