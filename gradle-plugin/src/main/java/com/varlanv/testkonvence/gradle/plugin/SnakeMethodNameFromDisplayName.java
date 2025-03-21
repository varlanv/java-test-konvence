package com.varlanv.testkonvence.gradle.plugin;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

@RequiredArgsConstructor
final class SnakeMethodNameFromDisplayName implements EnforceCandidate {

    private static final List<Function<String, String>> methodNameChain = Arrays.asList(
        // 1. Replace non-alphanumeric characters with underscores
        in -> in.replaceAll("[^a-zA-Z0-9]+", "_"),
        // 2. Remove leading and trailing underscores
        in -> in.replaceAll("^_|_$", ""),
        // 3. Convert to lowercase
        String::toLowerCase,
        // 4. Replace multiple consecutive underscores with a single underscore
        in -> in.replaceAll("__+", "_"),
        //5. Remove numbers from the start of the string if any
        in -> in.replaceAll("^\\d+", ""),
        //6. Replace again multiple consecutive underscores with a single underscore after number removal
        in -> in.replaceAll("__+", "_"),
        //7. Remove leading and trailing underscores once more in case numbers were at the start
        in -> in.replaceAll("^_|_$", "")
    );

    @Getter
    String displayName;
    @Getter
    String originalName;
    @NonFinal
    @Nullable
    String newName;

    @Override
    public String newName() {
        if (newName == null) {
            newName = methodNameChain.stream().reduce(
                displayName,
                (result, transformation) -> {
                    if (result.isEmpty()) {
                        return result;
                    }
                    return transformation.apply(result);
                },
                FunctionalUtil.throwingCombiner()
            );
        }
        return newName;
    }

    @Override
    public Kind kind() {
        return Kind.METHOD;
    }
}
