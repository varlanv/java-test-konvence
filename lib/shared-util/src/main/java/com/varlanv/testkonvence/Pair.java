package com.varlanv.testkonvence;

import java.util.Objects;
import org.jspecify.annotations.Nullable;

public interface Pair<T1, T2> {

    T1 left();

    T2 right();

    static <T1, T2> Pair<T1, T2> of(T1 left, T2 right) {
        return new Pair<>() {

            @Override
            public T1 left() {
                return left;
            }

            @Override
            public T2 right() {
                return right;
            }

            @Override
            public String toString() {
                return "Pair{" + "left=" + left + ", right=" + right + "}";
            }

            @Override
            public boolean equals(@Nullable Object obj) {
                if (!(obj instanceof Pair)) {
                    return false;
                } else if (obj == this) {
                    return true;
                }
                Pair<?, ?> p = (Pair<?, ?>) obj;
                return Objects.equals(left, p.left()) && Objects.equals(right, p.right());
            }

            @Override
            public int hashCode() {
                return Objects.hash(left, right);
            }
        };
    }
}
