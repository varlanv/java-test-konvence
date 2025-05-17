package com.varlanv.testkonvence.gradle.plugin;

import lombok.val;

class StringMatch {

    private final String line;
    private final String match;

    StringMatch(String line, String match) {
        this.line = line;
        this.match = match;
    }

    public ImmutableIntVector matchingIndexes() {
        if (match.isEmpty() || line.isEmpty()) {
            return IntVector.INSTANCE;
        }
        int idx = line.indexOf(match);
        if (idx == -1) {
            return IntVector.INSTANCE;
        }
        val vector = new IntVector(1).add(idx);
        while (idx != -1) {
            idx = line.indexOf(match, idx + 1);
            if (idx != -1) {
                vector.add(idx);
            }
        }
        return vector;
    }
}
