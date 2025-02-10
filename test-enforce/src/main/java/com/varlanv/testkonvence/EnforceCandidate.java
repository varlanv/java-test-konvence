package com.varlanv.testkonvence;

public interface EnforceCandidate {

    String displayName();

    String originalName();

    String newName();

    default Boolean isForReplacement() {
        var newName = newName();
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
