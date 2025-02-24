package com.varlanv.testkonvence.enforce;

import com.varlanv.testkonvence.Constants;
import com.varlanv.testkonvence.FunctionalUtil;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.val;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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
                    FunctionalUtil.throwingCombiner()
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
            .flatMap(item -> {
                val candidate = item.candidate();
                if (candidate.kind() == EnforceCandidate.Kind.CLASS) {
                    return Stream.empty();
                }
                return Stream.concat(
                    displayNameToMethodNameTransformations(item),
                    methodNameToDisplayNameTransformations(item)
                );
            })
            .reduce(
                Transformations.empty(),
                Transformations::register,
                FunctionalUtil.throwingCombiner()
            );
    }

    private Stream<Transformations.Transformation> methodNameToDisplayNameTransformations(EnforcementMeta.Item item) {
        val candidate = item.candidate();
        if (!trainOptions.reverseTransformation() || candidate.kind() != EnforceCandidate.Kind.METHOD || !candidate.displayName().isEmpty()) {
            return Stream.empty();
        }
        return Stream.of(
            Transformations.Transformation.of(
                item.sourceFile().lines(),
                item,
                sourceLines -> {
                    val displayName = new DisplayNameFromMethodName(candidate.originalName()).displayName();
                    val linesView = sourceLines.view();
                    val methodName = candidate.originalName();
                    for (int lineIdx = 0; lineIdx < linesView.size(); lineIdx++) {
                        val line = linesView.get(lineIdx);
                        val matchIdx = line.indexOf("void " + methodName + "(");
                        if (matchIdx != -1) {
                            val stringBuilder = new StringBuilder();
                            for (int i1 = 0; i1 < matchIdx; i1++) {
                                stringBuilder.append(" ");
                            }
                            int finalLineIdx = lineIdx;
                            return findIndexOfEmptyLine(lineIdx, linesView)
                                .map(emptyLineIdx -> {
                                    val displayNameAnnotation = stringBuilder.append("@DisplayName(\"").append(displayName).append("\")").toString();
                                    val sl = sourceLines.pushAbove(finalLineIdx, displayNameAnnotation);
                                    return handleDisplayNameImport(sl);
                                })
                                .orElse(sourceLines);
                        }
                    }

                    return sourceLines;
                }
            )
        );
    }

    private SourceLines handleDisplayNameImport(SourceLines sourceLines) {
        val linesView = sourceLines.view();
        val importJunitLines = new LinkedHashSet<IntObjectPair<String>>();
        val junitDisplayNameImport = "import org.junit.jupiter.api.DisplayName;";
        for (int lineIdx = 0; lineIdx < linesView.size(); lineIdx++) {
            val line = linesView.get(lineIdx);
            if (line.contains("class {") || line.contains("import org.junit.jupiter.api.*") || line.contains(junitDisplayNameImport)) {
                return sourceLines;
            } else if (line.contains("import org.junit.jupiter.api") || line.contains("org.junit.jupiter.params.ParameterizedTest")) {
                importJunitLines.add(IntObjectPair.of(lineIdx, line));
            }
        }
        if (importJunitLines.isEmpty()) {
            return sourceLines;
        }

        val junitImportPair = IntObjectPair.of(-1, junitDisplayNameImport);
        val sortedJunitImports = Stream.concat(importJunitLines.stream(), Stream.of(junitImportPair))
            .sorted(Comparator.comparing(IntObjectPair::right))
            .collect(Collectors.toList());
        val indexOfJunitDisplayNameImport = sortedJunitImports.indexOf(junitImportPair);
        if (indexOfJunitDisplayNameImport == 0) {
            val idx = sortedJunitImports.get(1).left();
            return sourceLines.pushAbove(idx, junitDisplayNameImport);
        } else {
            val idx = sortedJunitImports.get(indexOfJunitDisplayNameImport - 1).left();
            return sourceLines.pushAbove(idx, junitDisplayNameImport);
        }
    }

    private Optional<Integer> findIndexOfEmptyLine(int start, List<String> lines) {
        for (int i = start; i > 0; i--) {
            if (lines.get(i).trim().isEmpty()) {
                return Optional.of(i);
            }
        }
        return Optional.empty();
    }

    private Stream<Transformations.Transformation> displayNameToMethodNameTransformations(EnforcementMeta.Item item) {
        val candidate = item.candidate();
        val newName = candidate.newName();
        val originalName = candidate.originalName();

        if (newName.isEmpty() || originalName.equals(newName) || candidate.kind() != EnforceCandidate.Kind.METHOD) {
            return Stream.empty();
        }
        val sourceFile = item.sourceFile();
        val lines = sourceFile.lines();
        val linesView = lines.view();
        val methodNameMatches = new ArrayList<MethodNameMatch>(2);

        for (int lineIdx = 0, linesSize = linesView.size(); lineIdx < linesSize; lineIdx++) {
            val line = linesView.get(lineIdx);
            val matchIndexes = new StringMatch(line, originalName).matchingIndexes();
            if (matchIndexes.notEmpty()) {
                methodNameMatches.add(new MethodNameMatch(lineIdx, matchIndexes));
            }
        }
        if (methodNameMatches.isEmpty()) {
            return Stream.empty();
        }
        if (methodNameMatches.size() == 1) {
            val methodNameMatch = methodNameMatches.get(0);
            val matchIndexes = methodNameMatch.matchIndexes();
            if (matchIndexes.size() == 1) {
                return Stream.of(
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
                return Stream.of(
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
                    return Stream.of(
                        Transformations.Transformation.of(
                            lines,
                            item,
                            (sl) -> sl.replaceAt(indexOfClosestClassDistance, line -> line.replace(originalName, newName))
                        )
                    );
                }
            }
        }
        return Stream.empty();
    }


    @Value
    private static class MethodNameMatch {

        int lineIndex;
        ImmutableIntVector matchIndexes;
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
