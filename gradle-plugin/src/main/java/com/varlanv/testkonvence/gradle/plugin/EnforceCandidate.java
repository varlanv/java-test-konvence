package com.varlanv.testkonvence.gradle.plugin;

interface EnforceCandidate {

    String displayName();

    String originalName();

    String newName();

    Kind kind();

    enum Kind {

        CLASS,
        METHOD
    }
}
