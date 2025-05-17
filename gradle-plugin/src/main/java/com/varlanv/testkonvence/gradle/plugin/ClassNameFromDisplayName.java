package com.varlanv.testkonvence.gradle.plugin;

final class ClassNameFromDisplayName implements EnforceCandidate {

    private final String displayName;
    private final String originalName;

    ClassNameFromDisplayName(String displayName, String originalName) {
        this.displayName = displayName;
        this.originalName = originalName;
    }

    @Override
    public String displayName() {
        return displayName;
    }

    @Override
    public String originalName() {
        return originalName;
    }

    @Override
    public String newName() {
        if (displayName.isEmpty()) {
            return "";
        }

        var className = new StringBuilder();
        boolean capitalizeNext = true;
        boolean hasLetter = false;

        for (var ch : displayName.toCharArray()) {
            if (Character.isLetter(ch) && ch < 128) {
                hasLetter = true;
                if (capitalizeNext) {
                    className.append(Character.toUpperCase(ch));
                    capitalizeNext = false;
                } else {
                    className.append(Character.toLowerCase(ch));
                }
            } else if (Character.isDigit(ch) && hasLetter) {
                className.append(ch);
            } else {
                capitalizeNext = true;
            }
        }

        return className.toString();
    }

    @Override
    public Kind kind() {
        return Kind.CLASS;
    }
}
