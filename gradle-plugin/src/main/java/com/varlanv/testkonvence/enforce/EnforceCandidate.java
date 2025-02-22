package com.varlanv.testkonvence.enforce;

import lombok.val;

public interface EnforceCandidate {

    String displayName();

    String originalName();

    String newName();

    default Boolean isForReplacement() {
        val newName = newName();
        return !newName.isEmpty() && !originalName().equals(newName);
    }

    Kind kind();

    enum Kind {

        CLASS,
        METHOD
    }
}
