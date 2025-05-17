package com.varlanv.testkonvence.gradle.plugin;

import lombok.val;
import org.jetbrains.annotations.Nullable;

final class CamelMethodNameFromDisplayName implements EnforceCandidate {

    private final SnakeMethodNameFromDisplayName snake;

    @Nullable String newName;

    CamelMethodNameFromDisplayName(SnakeMethodNameFromDisplayName snake) {
        this.snake = snake;
    }

    @Override
    public String displayName() {
        return snake.displayName();
    }

    @Override
    public String originalName() {
        return snake.originalName();
    }

    @Override
    public String newName() {
        if (newName == null) {
            val split = snake.newName().split("_");
            val result = new StringBuilder();
            for (int i = 0; i < split.length; i++) {
                val part = split[i];
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
            newName = result.toString();
        }
        return newName;
    }

    @Override
    public Kind kind() {
        return Kind.METHOD;
    }
}
