package com.varlanv.testkonvence.info;

import lombok.Value;

import java.util.List;

@Value
public class EnforcementMeta {

    List<Item> items;

    @Value
    public static class Item {

        String fullEnclosingClassName;
        String displayName;
        String className;
        String methodName;
    }
}
