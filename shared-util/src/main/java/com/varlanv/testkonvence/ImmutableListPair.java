package com.varlanv.testkonvence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ImmutableListPair<T> {

    private final List<T> originalList = new ArrayList<>();
    private final List<T> immutableList = Collections.unmodifiableList(originalList);

    public void add(T item) {
        originalList.add(item);
    }

    public List<T> immutableList() {
        return immutableList;
    }
}
