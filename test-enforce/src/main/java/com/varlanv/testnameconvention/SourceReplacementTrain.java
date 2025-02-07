package com.varlanv.testnameconvention;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
                if (item.candidate().kind() == EnforceCandidate.Kind.METHOD) {
                    if (item.candidate().isForReplacement()) {
                        var newName = item.candidate().newName();
                        var originalName = item.candidate().originalName();
                        return new ActionSourceReplacementRule(
                            item.sourceFile(),
                            lines -> {
                                List<String> result = null;
                                for (int lineIdx = 0, linesSize = lines.size(); lineIdx < linesSize; lineIdx++) {
                                    var line = lines.get(lineIdx);
                                    if (line.contains("void " + originalName + "(")) {
                                        if (result == null) {
                                            result = new ArrayList<>(lines);
                                        }
                                        result.set(lineIdx, line.replace(originalName, newName));
                                    }
                                }
                                return result != null ? result : Collections.unmodifiableList(lines);
                            });
                    }
                }

                return new NoopSourceReplacementRule(item.sourceFile());
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
}
