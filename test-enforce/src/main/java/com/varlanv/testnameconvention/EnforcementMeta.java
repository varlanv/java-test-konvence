package com.varlanv.testnameconvention;

import lombok.Value;

import java.util.List;

@Value
public class EnforcementMeta {

    List<Item> items;

    @Value
    public static class Item {

        SourceFile sourceFile;
        String immediateClassName;
        EnforceCandidate candidate;
    }
}
