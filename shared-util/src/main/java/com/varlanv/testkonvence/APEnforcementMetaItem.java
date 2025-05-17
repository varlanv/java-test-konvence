package com.varlanv.testkonvence;

import org.immutables.value.Value;

@Value.Immutable
@Value.Style(strictBuilder = true)
public interface APEnforcementMetaItem {

    String fullEnclosingClassName();

    String displayName();

    String className();

    String methodName();
}
