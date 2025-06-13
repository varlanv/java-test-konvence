package com.varlanv.testkonvence.proc;

import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class SnakeMethodNameFromDisplayName {

    private SnakeMethodNameFromDisplayName() {}

    private static final Function<String, String>[] transformationsChain = Stream.of(
                    // 1. Replace non-alphanumeric characters with underscores
                    Map.entry("[^a-zA-Z0-9]+", "_"),
                    // 2. Remove leading and trailing underscores
                    Map.entry("^_|_$", ""),
                    // 4. Replace multiple consecutive underscores with a single underscore
                    Map.entry("__+", "_"),
                    // 5. Remove numbers from the start of the string if any
                    Map.entry("^\\d+", ""),
                    // 6. Replace again multiple consecutive underscores with a single underscore after number removal
                    Map.entry("__+", "_"),
                    // 7. Remove leading and trailing underscores once more in case numbers were at the start
                    Map.entry("^_|_$", ""))
            .map(entry -> {
                var pattern = Pattern.compile(entry.getKey());
                var replacement = entry.getValue();
                return (Function<String, String>)
                        value -> pattern.matcher(value).replaceAll(replacement);
            })
            .collect(Collectors.collectingAndThen(Collectors.toList(), result -> {
                result.add(s -> s.toLowerCase(Locale.ROOT));
                @SuppressWarnings("unchecked")
                var array = (Function<String, String>[]) result.toArray(new Function<?, ?>[0]);
                return array;
            }));

    static String convert(String displayName) {
        var res = displayName;
        for (var transform : transformationsChain) {
            res = transform.apply(res);
        }
        return res;
    }
}
