package com.varlanv.testkonvence.proc;

import com.varlanv.testkonvence.*;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedSet;

final class APIntermediateOutput {

    private final Map<String, EnforcementsRecord> enforcements = new LinkedHashMap<>();
    private final ImmutableSortedSetPair<APEnforcementTop> topLevelEnforcements = new ImmutableSortedSetPair<>(
        Comparator.comparing(APEnforcementTop::fullEnclosingClassName)
    );

    public void add(APEnforcementFull fullItem) {
        var fullEnclosingClassName = fullItem.fullEnclosingClassName();
        var enforcementsRecord = enforcements.get(fullEnclosingClassName);
        var className = fullItem.className();
        if (enforcementsRecord == null) {
            enforcementsRecord = new EnforcementsRecord();
            enforcements.put(fullEnclosingClassName, enforcementsRecord);
            var items = newSortedSetForItems();
            var item = buildItem(fullItem);
            items.add(item);
            enforcementsRecord.classMap.put(className, items);
            var middleItem = ImmutableAPEnforcementMiddle.of(className, items.immutableSet());
            enforcementsRecord.classSet.add(middleItem);

            topLevelEnforcements.add(
                ImmutableAPEnforcementTop.of(
                    fullEnclosingClassName,
                    enforcementsRecord.classSet.immutableSet())
            );
        } else {
            var itemsList = enforcementsRecord.classMap.get(className);
            if (itemsList == null) {
                itemsList = newSortedSetForItems();
                enforcementsRecord.classMap.put(className, itemsList);
                var middleItem = ImmutableAPEnforcementMiddle.of(className, itemsList.immutableSet());
                enforcementsRecord.classSet.add(middleItem);
                itemsList.add(buildItem(fullItem));
            } else {
                itemsList.add(buildItem(fullItem));
            }
        }
    }

    SortedSet<APEnforcementTop> items() {
        return topLevelEnforcements.immutableSet();
    }

    boolean isEmpty() {
        return enforcements.isEmpty();
    }

    private static ImmutableSortedSetPair<APEnforcementItem> newSortedSetForItems() {
        return new ImmutableSortedSetPair<>(Comparator.comparing(APEnforcementItem::originalName)
            .thenComparing(APEnforcementItem::displayName)
            .thenComparing(APEnforcementItem::newName));
    }

    private static final class EnforcementsRecord {

        private final ImmutableSortedSetPair<APEnforcementMiddle> classSet = new ImmutableSortedSetPair<>(
            Comparator.comparing(APEnforcementMiddle::className)
        );
        private final Map<String, ImmutableSortedSetPair<APEnforcementItem>> classMap = new LinkedHashMap<>();
    }

    private static APEnforcementItem buildItem(APEnforcementFull fullItem) {
        return ImmutableAPEnforcementItem.builder()
            .displayName(fullItem.displayName())
            .originalName(fullItem.originalName())
            .newName(fullItem.newName())
            .build();
    }
}
