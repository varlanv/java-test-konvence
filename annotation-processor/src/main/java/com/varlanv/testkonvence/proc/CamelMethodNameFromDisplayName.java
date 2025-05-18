package com.varlanv.testkonvence.proc;

final class CamelMethodNameFromDisplayName {

    static String convert(String displayName) {
        var split = SnakeMethodNameFromDisplayName.convert(displayName).split("_");
        var result = new StringBuilder();
        for (int i = 0; i < split.length; i++) {
            var part = split[i];
            if (!part.isEmpty()) {
                if (i == 0) {
                    result.append(split[i]);
                } else {
                    if (part.length() == 1) {
                        result.append(part.toUpperCase());
                    } else {
                        result.append(part.substring(0, 1).toUpperCase()).append(part.substring(1));
                    }
                }
            }
        }
        return result.toString();
    }
}
