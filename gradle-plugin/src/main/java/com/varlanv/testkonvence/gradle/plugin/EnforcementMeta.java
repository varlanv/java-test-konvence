package com.varlanv.testkonvence.gradle.plugin;

import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

final class EnforcementMeta {

    private final List<Item> items;

    EnforcementMeta(List<Item> items) {
        this.items = items;
    }

    public List<Item> items() {
        return items;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (!(o instanceof EnforcementMeta)) {
            return false;
        }
        EnforcementMeta that = (EnforcementMeta) o;
        return Objects.equals(items, that.items);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(items);
    }

    static class Item {

        private final SourceFile sourceFile;
        private final String immediateClassName;
        private final EnforceCandidate candidate;

        public Item(SourceFile sourceFile, String immediateClassName, EnforceCandidate candidate) {
            this.sourceFile = sourceFile;
            this.immediateClassName = immediateClassName;
            this.candidate = candidate;
        }

        public SourceFile sourceFile() {
            return sourceFile;
        }

        public String immediateClassName() {
            return immediateClassName;
        }

        public EnforceCandidate candidate() {
            return candidate;
        }

        @Override
        public boolean equals(@Nullable Object o) {
            if (!(o instanceof Item)) {
                return false;
            }
            Item item = (Item) o;
            return Objects.equals(sourceFile, item.sourceFile)
                    && Objects.equals(immediateClassName, item.immediateClassName)
                    && Objects.equals(candidate, item.candidate);
        }

        @Override
        public int hashCode() {
            return Objects.hash(sourceFile, immediateClassName, candidate);
        }
    }
}
