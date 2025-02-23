package com.varlanv.testkonvence.enforce;

import org.jetbrains.annotations.Range;

import java.util.function.IntConsumer;

public interface ImmutableIntVector {

    ImmutableIntVector INSTANCE = new ImmutableIntVector() {

        @Override
        public void forEach(IntConsumer action) {
            // no-op
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public int get(int index) {
            throw new IndexOutOfBoundsException();
        }

        @Override
        public int first() {
            throw new IndexOutOfBoundsException();
        }
    };

    void forEach(IntConsumer action);

    @Range(from = 0, to = Integer.MAX_VALUE)
    int size();

    default boolean empty() {
        return size() == 0;
    }

    default boolean notEmpty() {
        return !empty();
    }

    int get(@Range(from = 0, to = Integer.MAX_VALUE) int index);

    int first();
}
