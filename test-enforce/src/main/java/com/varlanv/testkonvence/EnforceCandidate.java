package com.varlanv.testkonvence;

import lombok.val;

public interface EnforceCandidate {

    String displayName();

    String originalName();

    String newName();

    default Boolean isForReplacement() {
        val newName = newName();
        if (newName.isEmpty()) {
            return false;
        } else {
            return !originalName().equals(newName);
        }
    }

    Kind kind();

    enum Kind {

        CLASS,
        METHOD
    }
}
