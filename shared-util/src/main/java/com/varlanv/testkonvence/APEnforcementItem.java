package com.varlanv.testkonvence;

import java.util.Comparator;
import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;

@Value.Immutable
@Value.Style(strictBuilder = true)
public interface APEnforcementItem extends Comparable<APEnforcementItem> {

    Comparator<APEnforcementItem> COMPARATOR = Comparator.comparing(APEnforcementItem::displayName)
            .thenComparing(APEnforcementItem::originalName)
            .thenComparing(APEnforcementItem::newName);

    String displayName();

    String originalName();

    String newName();

    @Override
    default int compareTo(@NotNull APEnforcementItem o) {
        return COMPARATOR.compare(this, o);
    }
}
