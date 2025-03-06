package com.varlanv.testkonvence.gradle.plugin;

import lombok.Value;

import java.util.List;

@Value
class EnforcementMeta {

    List<Item> items;

    @Value
    public static class Item {

        SourceFile sourceFile;
        String immediateClassName;
        EnforceCandidate candidate;
    }
}
