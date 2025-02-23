package com.varlanv.testkonvence.enforce;

public interface EnforceCandidate {

    String displayName();

    String originalName();

    String newName();

    Kind kind();

    enum Kind {

        CLASS,
        METHOD
    }
}
