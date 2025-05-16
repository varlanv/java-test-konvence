package com.varlanv.testkonvence.gradle.plugin;

import java.util.List;
import lombok.Value;

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
