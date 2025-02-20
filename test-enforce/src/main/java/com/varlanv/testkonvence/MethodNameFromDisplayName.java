package com.varlanv.testkonvence;

import lombok.Value;
import lombok.val;
import lombok.var;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

@Value
public class MethodNameFromDisplayName implements EnforceCandidate {

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

    String displayName;
    String methodName;

    @Override
    public String originalName() {
        return methodName;
    }

    @Override
    public String newName() {
        var result = displayName;
        for (val transformation : methodNameChain) {
            if (result.isEmpty()) {
                return result;
            }
            result = transformation.apply(result);

        }
        if (result.isEmpty()) {
            return "";
        }
        return result;
    }

    @Override
    public Kind kind() {
        return Kind.METHOD;
    }
}
