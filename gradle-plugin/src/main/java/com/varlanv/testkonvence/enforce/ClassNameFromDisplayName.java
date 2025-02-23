package com.varlanv.testkonvence.enforce;

import lombok.Value;
import lombok.val;

@Value
public class ClassNameFromDisplayName implements EnforceCandidate {

    String displayName;
    String originalName;

    @Override
    public String newName() {
        if (displayName.isEmpty()) {
            return "";
        }

        val className = new StringBuilder();
        boolean capitalizeNext = true;
        boolean hasLetter = false;

        for (val ch : displayName.toCharArray()) {
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
