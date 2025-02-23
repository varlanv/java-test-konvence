package com.varlanv.testkonvence.enforce;

public interface IntObjectPair<T> {

    int left();

    T right();

    static <T> IntObjectPair<T> of(int left, T right) {
        return new IntObjectPair<T>() {

            @Override
            public int left() {
                return left;
            }

            @Override
            public T right() {
                return right;
            }
        };
    }
}
