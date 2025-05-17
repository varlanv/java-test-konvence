package com.varlanv.testkonvence.gradle.plugin;

import java.util.Objects;
import org.checkerframework.checker.nullness.qual.Nullable;

final class IntObjectPair<T> {

    private final int left;
    private final T right;

    private IntObjectPair(int left, T right) {
        this.left = left;
        this.right = right;
    }

    public int left() {
        return left;
    }

    public T right() {
        return right;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (!(o instanceof IntObjectPair)) {
            return false;
        }
        IntObjectPair<?> that = (IntObjectPair<?>) o;
        return left == that.left && Objects.equals(right, that.right);
    }

    @Override
    public int hashCode() {
        return Objects.hash(left, right);
    }

    static <T> IntObjectPair<T> of(int left, T right) {
        return new IntObjectPair<T>(left, right);
    }
}
