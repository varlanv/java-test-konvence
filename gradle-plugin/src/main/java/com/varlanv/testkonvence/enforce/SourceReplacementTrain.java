package com.varlanv.testkonvence.enforce;

import com.varlanv.testkonvence.Constants;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.val;

import java.util.*;
import java.util.stream.IntStream;

@RequiredArgsConstructor
public class SourceReplacementTrain {

    TrainOptions trainOptions;
    EnforcementMeta enforcementMeta;

    public void run() {
        transformations().consumeGroupedByFile((target, transformations) -> {
            val resultLines = IntStream.range(0, transformations.size())
                .mapToObj(i -> IntObjectPair.of(i, transformations.get(i)))
                .sorted((a, b) -> {
                    if (a.right().input().meta().candidate().newName().equals(b.right().input().meta().candidate().originalName())) {
                        return 1;
                    } else {
                        return Integer.compare(a.left(), b.left());
                    }
                })
                .reduce(
                    target.lines(),
                    (lines, transformation) -> transformation.right().action().apply(lines),
                    (a, b) -> b
                );
            if (resultLines.changed()) {
                if (trainOptions.dryWithFailing()) {
                    throw new IllegalStateException(
                        String.format(
                            "[%s] - found test name mismatch in file [%s]",
                            Constants.PLUGIN_NAME, target.path().toAbsolutePath()
                        )
                    );
                } else {
                    target.save(resultLines);
                }
            }
        });
    }

    private Transformations transformations() {
        return enforcementMeta.items().stream()
            .reduce(
                Transformations.empty(),
                (resultTransformations, item) -> {
                    if (!item.candidate().isForReplacement()) {
                        return resultTransformations;
                    }
                    if (item.candidate().kind() == EnforceCandidate.Kind.METHOD) {
                        return methodReplacementRule(item, resultTransformations);
                    } else {
                        return resultTransformations;
                    }

                },
                (a, b) -> b
            );
    }


    @Value
    private static class MethodNameMatch {

        int lineIndex;
        ImmutableIntVector matchIndexes;
    }

    private Transformations methodReplacementRule(EnforcementMeta.Item item, Transformations transformations) {
        val sourceFile = item.sourceFile();
        val lines = sourceFile.lines();
        val linesView = lines.view();
        val candidate = item.candidate();
        val newName = candidate.newName();
        val originalName = candidate.originalName();
        val methodNameMatches = new ArrayList<MethodNameMatch>(2);

        for (int lineIdx = 0, linesSize = linesView.size(); lineIdx < linesSize; lineIdx++) {
            val line = linesView.get(lineIdx);
            val matchIndexes = new StringMatch(line, originalName).matchingIndexes();
            if (matchIndexes.notEmpty()) {
                methodNameMatches.add(new MethodNameMatch(lineIdx, matchIndexes));
            }
        }
        if (methodNameMatches.isEmpty()) {
            return transformations;
        }
        if (methodNameMatches.size() == 1) {
            val methodNameMatch = methodNameMatches.get(0);
            val matchIndexes = methodNameMatch.matchIndexes();
            if (matchIndexes.size() == 1) {
                return transformations.register(
                    Transformations.Transformation.of(
                        lines,
                        item,
                        (sl) -> sl.replaceAt(methodNameMatch.lineIndex(), line -> line.replace(originalName, newName))
                    )
                );
            }
        } else {
            val finalIndexes = new ArrayList<Integer>(methodNameMatches.size());
            methodNameMatches.forEach(match -> {
                val line = linesView.get(match.lineIndex());
                if (line.contains("void " + originalName + "(")) {
                    finalIndexes.add(match.lineIndex());
                }
            });
            if (finalIndexes.size() == 1) {
                return transformations.register(
                    Transformations.Transformation.of(
                        lines,
                        item,
                        (sl) -> sl.replaceAt(finalIndexes.get(0), line -> line.replace("void " + originalName + "(", "void " + newName + "("))
                    )
                );
            } else {
                val maybeIndexOfClosestClassDistance = findIndexOfClosestClassDistance(item, linesView, finalIndexes);
                if (maybeIndexOfClosestClassDistance.isPresent()) {
                    val indexOfClosestClassDistance = maybeIndexOfClosestClassDistance.get();
                    return transformations.register(
                        Transformations.Transformation.of(
                            lines,
                            item,
                            (sl) -> sl.replaceAt(indexOfClosestClassDistance, line -> line.replace(originalName, newName))
                        )
                    );
                }
            }
        }
        return transformations;
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
