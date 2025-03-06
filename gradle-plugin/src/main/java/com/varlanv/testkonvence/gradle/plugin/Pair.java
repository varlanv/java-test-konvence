package com.varlanv.testkonvence.gradle.plugin;

interface Pair<T1, T2> {

    T1 left();

    T2 right();

    static <T1, T2> Pair<T1, T2> of(T1 left, T2 right) {

        return new Pair<T1, T2>() {

            @Override
            public T1 left() {
                return left;
            }

            @Override
            public T2 right() {
                return right;
            }
        };
    }
}
