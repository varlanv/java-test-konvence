package com.varlanv.testkonvence.proc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class DisplayNameFromMethodName {

    public static String convert(String originalMethodName) {
        var methodName = preTransform(originalMethodName);
        if (methodName.length() == 1) {
            return methodName;
        }
        var snakeSplit = methodName.split("_");
        var isFullCamelCase = snakeSplit.length == 1;
        if (isFullCamelCase) {
            return String.join(" ", splitCamelToWords(methodName));
        } else {
            return Arrays.stream(snakeSplit)
                .map(word -> {
                    var words = splitCamelToWords(word);
                    if (words.size() > 1) {
                        return "'" + word + "'";
                    }
                    return word;
                })
                .collect(Collectors.joining(" "));
        }
    }

    private static List<String> splitCamelToWords(String input) {
        var words = new ArrayList<String>();
        var currentWord = new StringBuilder();

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (Character.isUpperCase(c) && currentWord.length() > 0) {
                words.add(currentWord.toString().toLowerCase());
                currentWord.setLength(0);
            }
            currentWord.append(c);
        }

        words.add(currentWord.toString().toLowerCase());

        return words;
    }

    private static String preTransform(String methodName) {
        if (methodName.length() == 1) {
            return methodName;
        }

        var result = new StringBuilder();
        boolean previousUnderscore = false;

        for (int i = 0; i < methodName.length(); i++) {
            char c = methodName.charAt(i);

            if (c == '_') {
                if (!previousUnderscore && result.length() > 0) {
                    result.append(c);
                    previousUnderscore = true;
                }
            } else {
                result.append(c);
                previousUnderscore = false;
            }
        }

        return result.toString();
    }
}
