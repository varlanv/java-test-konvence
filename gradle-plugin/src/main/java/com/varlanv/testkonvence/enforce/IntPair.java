package com.varlanv.testkonvence.enforce;

public interface IntPair {

    int left();

    int right();

    static IntPair of(int left, int right) {
        return new IntPair() {
            @Override
            public int left() {
                return left;
            }

            @Override
            public int right() {
                return right;
            }
        };
    }
}
