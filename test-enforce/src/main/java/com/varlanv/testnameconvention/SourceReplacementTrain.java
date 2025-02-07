package com.varlanv.testnameconvention;

import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class SourceReplacementTrain {

    EnforcementMeta enforcementMeta;

    public void run() {
        rules().forEach(rule -> {
            var target = rule.target();
            var originalLines = target.lines();
            var newLines = rule.apply(originalLines);
            target.save(newLines);
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
                    return classReplacementRule(item);
                }

            })
            .filter(Predicate.not(SourceReplacementRule::isNoop))
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
                                for (var rules : map.values()) {
                                    for (var rule : rules) {
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
        var candidate = item.candidate();
        var newName = candidate.newName();
        var originalName = candidate.originalName();
        return new ActionSourceReplacementRule(
            item.sourceFile(),
            lines -> {
                var matchedLineIndexes = new ArrayList<Integer>(2);
                for (int lineIdx = 0, linesSize = lines.size(); lineIdx < linesSize; lineIdx++) {
                    var line = lines.get(lineIdx);
                    if (line.contains("void " + originalName + "(")) {
                        matchedLineIndexes.add(lineIdx);
                    }
                }
                if (matchedLineIndexes.isEmpty()) {
                    return lines;
                }
                var result = new ArrayList<>(lines);
                if (matchedLineIndexes.size() == 1) {
                    int idx = matchedLineIndexes.get(0);
                    var line = lines.get(idx);
                    result.set(idx, line.replace(originalName, newName));
                } else {
                    findIndexOfClosestClassDistance(item, lines, matchedLineIndexes).ifPresent(lineIndex -> {
                        var line = lines.get(lineIndex);
                        result.set(lineIndex, line.replace(originalName, newName));
                    });
                }
                return Collections.unmodifiableList(result);
            });
    }

    private SourceReplacementRule classReplacementRule(EnforcementMeta.Item item) {
        var candidate = new TestClassNameWithEnding(item.candidate());
        var newName = candidate.newName();
        var originalName = candidate.originalName();
        return new ActionSourceReplacementRule(
            item.sourceFile(),
            lines -> {
                var matchedLineIndexes = new ArrayList<Integer>(2);
                for (int lineIdx = 0, linesSize = lines.size(); lineIdx < linesSize; lineIdx++) {
                    var line = lines.get(lineIdx);
                    if (line.contains("class " + originalName + " {")) {
                        matchedLineIndexes.add(lineIdx);
                    }
                }
                if (matchedLineIndexes.isEmpty()) {
                    return lines;
                }
                var result = new ArrayList<>(lines);
                if (matchedLineIndexes.size() == 1) {
                    int idx = matchedLineIndexes.get(0);
                    var line = lines.get(idx);
                    result.set(idx, line.replace(originalName, newName));
                } else {
                    findIndexOfClosestClassDistance(item, lines, matchedLineIndexes).ifPresent(lineIndex -> {
                        var line = lines.get(lineIndex);
                        result.set(lineIndex, line.replace(originalName, newName));
                    });
                }
                return Collections.unmodifiableList(result);
            });
    }

    private Optional<Integer> findIndexOfClosestClassDistance(EnforcementMeta.Item item,
                                                              List<String> lines,
                                                              ArrayList<Integer> matchedLineIndexes) {
        var lineIndexToOuterClassDistance = new TreeMap<Integer, Integer>();
        var targetClassChunk = "class " + item.immediateClassName() + " {";
        for (var matchedLineIndex : matchedLineIndexes) {
            var distance = 0;
            for (int idx = matchedLineIndex; idx >= 0; idx--) {
                var line = lines.get(idx);
                if (line.contains(targetClassChunk)) {
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
