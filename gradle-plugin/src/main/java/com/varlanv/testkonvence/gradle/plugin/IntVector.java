package com.varlanv.testkonvence.gradle.plugin;

import java.util.Arrays;
import java.util.function.IntConsumer;

final class IntVector implements ImmutableIntVector {

    int[] array;
    int index = 0;

    IntVector(int initialCapacity) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Initial capacity must be non-negative");
        }
        this.array = new int[initialCapacity];
    }

    public IntVector add(int value) {
        ensureCapacity();
        array[index++] = value;
        return this;
    }

    @Override
    public void forEach(IntConsumer action) {
        for (int i = 0; i < index; i++) {
            action.accept(array[i]);
        }
    }

    @Override
    public int size() {
        return index;
    }

    @Override
    public int get(int index) {
        return array[index];
    }

    @Override
    public int first() {
        if (index == 0) {
            throw new ArrayIndexOutOfBoundsException();
        }
        return array[0];
    }

    private void ensureCapacity() {
        if (index >= array.length) {
            array = Arrays.copyOf(array, (array.length == 0 ? 8 : array.length) * 2);
        }
    }
}
