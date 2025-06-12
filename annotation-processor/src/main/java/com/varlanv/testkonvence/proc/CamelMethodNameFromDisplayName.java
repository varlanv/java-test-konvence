package com.varlanv.testkonvence.proc;

import java.util.Locale;

final class CamelMethodNameFromDisplayName {

    private CamelMethodNameFromDisplayName() {}

    static String convert(String displayName) {
        var snakeName = SnakeMethodNameFromDisplayName.convert(displayName);
        var result = new StringBuilder(snakeName.length());
        var split = snakeName.split("_", -1);
        for (var i = 0; i < split.length; i++) {
            var part = split[i];
            if (!part.isEmpty()) {
                if (i == 0) {
                    result.append(split[i]);
                } else {
                    if (part.length() == 1) {
                        result.append(part.toUpperCase(Locale.ROOT));
                    } else {
                        result.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
                    }
                }
            }
        }
        return result.toString();
    }
}
