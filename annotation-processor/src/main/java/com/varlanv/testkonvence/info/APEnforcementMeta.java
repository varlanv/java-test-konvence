package com.varlanv.testkonvence.info;

import java.util.List;
import lombok.Value;

@Value
public class APEnforcementMeta {

    List<Item> items;

    @Value
    public static class Item {

        String fullEnclosingClassName;
        String displayName;
        String className;
        String methodName;
    }
}
