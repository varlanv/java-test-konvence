package com.varlanv.testkonvence.enforce;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class SourceReplacementTrain {

    EnforcementMeta enforcementMeta;

    @SneakyThrows
    private String resolveLineSeparator(Path path, List<String> lines) {
        if (lines.isEmpty()) {
            return "";
        }
        val content = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        if (content.startsWith(lines.get(0) + "\r\n")) {
            return "\r\n";
        } else {
            return "\n";
        }
    }

    public void run() {
        rules().forEach(rule -> {
            val target = rule.target();
            val originalLines = target.lines();
            val newLines = rule.apply(originalLines);
            target.save(newLines, resolveLineSeparator(target.path(), originalLines));
        });
    }

    private Stream<SourceReplacementRule> rules() {
        return enforcementMeta.items().stream()
            .map(item -> {
                if (!item.candidate().isForReplacement()) {
                    return new NoopSourceReplacementRule(item.sourceFile());
                }
                if (item.candidate().kind() == EnforceCandidate.Kind.METHOD) {
                    return methodReplacementRule(item);
                } else {
                    return new NoopSourceReplacementRule(item.sourceFile());
                }

            })
            .filter(sourceReplacementRule -> !sourceReplacementRule.isNoop())
            .collect(
                Collectors.collectingAndThen(
                    Collectors.groupingBy(rule -> rule.target().path()),
                    map -> map.values().stream()
                        .map(sourceReplacementRules -> {
                            if (sourceReplacementRules.stream().allMatch(SourceReplacementRule::isNoop)) {
                                return new NoopSourceReplacementRule(sourceReplacementRules.get(0).target());
                            }
                            Function<List<String>, List<String>> mergedReplacementAction = lines -> {
                                List<String> resultLines = lines;
                                for (val rules : map.values()) {
                                    for (val rule : rules) {
                                        resultLines = rule.apply(resultLines);
                                    }
                                }
                                return resultLines;
                            };
                            return new ActionSourceReplacementRule(sourceReplacementRules.get(0).target(), mergedReplacementAction);
                        })
                )
            );
    }

    private SourceReplacementRule methodReplacementRule(EnforcementMeta.Item item) {
        val candidate = item.candidate();
        val newName = candidate.newName();
        val originalName = candidate.originalName();
        return new ActionSourceReplacementRule(
            item.sourceFile(),
            lines -> {
                val matchedLineIndexes = new ArrayList<Integer>(2);
                for (int lineIdx = 0, linesSize = lines.size(); lineIdx < linesSize; lineIdx++) {
                    val line = lines.get(lineIdx);
                    if (line.contains(originalName)) {
                        matchedLineIndexes.add(lineIdx);
                    }
                }
                if (matchedLineIndexes.isEmpty()) {
                    return lines;
                }
                val result = new ArrayList<>(lines);
                if (matchedLineIndexes.size() == 1) {
                    val idx = matchedLineIndexes.get(0);
                    val line = lines.get(idx);
                    result.set(idx, line.replace(originalName, newName));
                } else {
                    val finalIndexes = new ArrayList<Integer>(matchedLineIndexes.size());
                    for (val idx : matchedLineIndexes) {
                        val line = lines.get(idx);
                        if (line.contains(originalName + "(")) {
                            finalIndexes.add(idx);
                        }
                    }
                    if (finalIndexes.size() == 1) {
                        result.set(finalIndexes.get(0), lines.get(finalIndexes.get(0)).replace(originalName, newName));
                    } else {
                        findIndexOfClosestClassDistance(item, lines, finalIndexes).ifPresent(lineIndex -> {
                            val line = lines.get(lineIndex);
                            result.set(lineIndex, line.replace(originalName, newName));
                        });
                    }
                }
                return Collections.unmodifiableList(result);
            });
    }

    private Optional<Integer> findIndexOfClosestClassDistance(EnforcementMeta.Item item,
                                                              List<String> lines,
                                                              List<Integer> matchedLineIndexes) {
        val lineIndexToOuterClassDistance = new TreeMap<Integer, Integer>();
        val immediateClassName = item.immediateClassName();
        val targetClassChunk = "class " + immediateClassName + " {";
        val targetInterfaceChunk = "interface " + immediateClassName + " {";
        for (val matchedLineIndex : matchedLineIndexes) {
            int distance = 0;
            for (int idx = matchedLineIndex; idx >= 0; idx--) {
                val line = lines.get(idx);
                if (line.contains(immediateClassName) && (line.contains(targetClassChunk) || line.contains(targetInterfaceChunk))) {
                    lineIndexToOuterClassDistance.put(matchedLineIndex, distance);
                    break;
                }
                distance++;
            }
        }
        return Optional.ofNullable(lineIndexToOuterClassDistance.firstEntry())
            .map(Map.Entry::getKey);
    }
}
