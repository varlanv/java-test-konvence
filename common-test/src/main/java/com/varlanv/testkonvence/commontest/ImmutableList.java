package com.varlanv.testkonvence.commontest;

import java.util.*;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ImmutableList<T> implements Iterable<@NonNull T> {

    private final List<@NonNull T> value;

    private ImmutableList(List<@NonNull T> value) {
        this.value = value;
    }

    public static <T> ImmutableList<T> copyOf(Collection<T> collection) {
        var result = new ArrayList<@NonNull T>(collection.size());
        for (var t : collection) {
            if (t == null) {
                throw new IllegalArgumentException();
            }
            result.add(t);
        }
        return new ImmutableList<>(result);
    }

    @SafeVarargs
    @SuppressWarnings("varargs")
    public static <T> ImmutableList<T> of(T... value) {
        return copyOf(Arrays.asList(value));
    }

    public List<@NonNull T> value() {
        return Collections.unmodifiableList(value);
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
    public @NotNull Iterator<@NonNull T> iterator() {
        return value.iterator();
    }
}
