package com.varlanv.testkonvence;

import org.immutables.value.Value;

@Value.Immutable
public interface APEnforcementMeta {

    ImmutableList<APEnforcementMetaItem> items();
}
