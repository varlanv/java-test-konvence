package com.varlanv.testkonvence;

import java.util.Collections;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

public class ImmutableSortedSetPair<T> {

    private final SortedSet<T> originalSet;
    private final SortedSet<T> immutableSet;

    public ImmutableSortedSetPair(Comparator<T> comparator) {
        this.originalSet = new TreeSet<>(comparator);
        this.immutableSet = Collections.unmodifiableSortedSet(originalSet);
    }

    public void add(T item) {
        originalSet.add(item);
    }

    public SortedSet<T> immutableSet() {
        return immutableSet;
    }
}
