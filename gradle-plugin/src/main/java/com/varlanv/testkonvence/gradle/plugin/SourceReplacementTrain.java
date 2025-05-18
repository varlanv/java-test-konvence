package com.varlanv.testkonvence.gradle.plugin;

import com.varlanv.testkonvence.Constants;
import com.varlanv.testkonvence.TrustedException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.slf4j.Logger;

final class SourceReplacementTrain {

    private final Logger log;
    private final TrainOptions trainOptions;
    private final EnforcementMeta enforcementMeta;

    SourceReplacementTrain(Logger log, TrainOptions trainOptions, EnforcementMeta enforcementMeta) {
        this.log = log;
        this.trainOptions = trainOptions;
        this.enforcementMeta = enforcementMeta;
    }

    public void run() throws Exception {
        transformations().consumeGroupedByFile((target, transformations) -> {
            var resultLines = IntStream.range(0, transformations.size())
                    .mapToObj(i -> IntObjectPair.of(i, transformations.get(i)))
                    .sorted((a, b) -> {
                        if (a.right()
                                .input()
                                .meta()
                                .candidate()
                                .newName()
                                .equals(b.right().input().meta().candidate().originalName())) {
                            return 1;
                        } else {
                            return Integer.compare(a.left(), b.left());
                        }
                    })
                    .reduce(
                            target.lines(),
                            (lines, transformation) ->
                                    transformation.right().action().apply(lines),
                            FunctionalUtil.throwingCombiner());
            if (resultLines.changed()) {
                if (trainOptions.dryWithFailing()) {
                    throw new TrustedException(String.format(
                            "[%s] - found test naming mismatch in file [%s]. "
                                    + "Consider running `testKonvenceEnforceAll` "
                                    + "Gradle task to apply test naming transformations.",
                            Constants.PLUGIN_NAME, target.path().toAbsolutePath()));
                } else {
                    try {
                        target.save(resultLines);
                    } catch (Exception e) {
                        log.debug(
                                "Failed to apply transformation to file [{}], ignoring exception",
                                target.path().toAbsolutePath());
                    }
                }
            }
        });
    }

    private Transformations transformations() {
        return enforcementMeta.items().stream()
                .flatMap(item -> {
                    var candidate = item.candidate();
                    if (candidate.kind() == EnforceCandidate.Kind.CLASS) {
                        return Stream.<Transformation>empty();
                    }
                    return Stream.concat(
                            displayNameToMethodNameTransformations(item), methodNameToDisplayNameTransformations(item));
                })
                .reduce(Transformations.empty(), Transformations::register, FunctionalUtil.throwingCombiner());
    }

    private Stream<Transformation> methodNameToDisplayNameTransformations(EnforcementMeta.Item item) {
        var candidate = item.candidate();
        if (!trainOptions.reverseTransformation()
                || candidate.kind() != EnforceCandidate.Kind.METHOD
                || !candidate.displayName().isEmpty()) {
            return Stream.empty();
        }
        return Stream.of(Transformation.of(item.sourceFile().lines(), item, sourceLines -> {
            var displayName = new DisplayNameFromMethodName(candidate.originalName()).displayName();
            var linesView = sourceLines.view();
            var methodName = candidate.originalName();
            var matchStr = "void " + methodName + "(";
            var matchedLineIndexes = new IntVector(3);
            var linesSize = linesView.size();
            for (int lineIdx = 0; lineIdx < linesSize; lineIdx++) {
                var line = linesView.get(lineIdx);
                var matchIdx = line.indexOf(matchStr);
                if (matchIdx != -1) {
                    matchedLineIndexes.add(lineIdx);
                }
            }
            if (matchedLineIndexes.size() == 0) {
                return sourceLines;
            } else if (matchedLineIndexes.size() == 1) {
                var lineIdx = matchedLineIndexes.get(0);
                var line = linesView.get(lineIdx);
                var matchIdx = line.indexOf(matchStr);
                var stringBuilder = new StringBuilder();
                stringBuilder.append(" ".repeat(Math.max(0, matchIdx)));
                return findIndexOfEmptyLine(lineIdx, linesView)
                        .map(emptyLineIdx -> {
                            var displayNameAnnotation = stringBuilder
                                    .append("@DisplayName(\"")
                                    .append(displayName)
                                    .append("\")")
                                    .toString();
                            var sl = sourceLines.pushAbove(lineIdx, displayNameAnnotation);
                            return handleDisplayNameImport(sl);
                        })
                        .orElse(sourceLines);
            } else {
                var maybeIndexOfClosestClassDistance =
                        findIndexOfClosestClassDistance(item, linesView, matchedLineIndexes);
                if (maybeIndexOfClosestClassDistance.isPresent()) {
                    int indexOfClosestClassDistance = maybeIndexOfClosestClassDistance.get();
                    var line = linesView.get(indexOfClosestClassDistance);
                    var matchIdx = line.indexOf(matchStr);
                    var displayNameAnnotation =
                            " ".repeat(Math.max(0, matchIdx)) + "@DisplayName(\"" + displayName + "\")";
                    var sl = sourceLines.pushAbove(indexOfClosestClassDistance, displayNameAnnotation);
                    return handleDisplayNameImport(sl);
                }
            }
            return sourceLines;
        }));
    }

    private SourceLines handleDisplayNameImport(SourceLines sourceLines) {
        var linesView = sourceLines.view();
        var importJunitLines = new LinkedHashSet<IntObjectPair<String>>();
        var junitDisplayNameImport = "import org.junit.jupiter.api.DisplayName;";
        for (int lineIdx = 0; lineIdx < linesView.size(); lineIdx++) {
            var line = linesView.get(lineIdx);
            if (line.contains("class {")
                    || line.contains("import org.junit.jupiter.api.*")
                    || line.contains(junitDisplayNameImport)) {
                return sourceLines;
            } else if (line.contains("import org.junit.jupiter.api")
                    || line.contains("org.junit.jupiter.params.ParameterizedTest")) {
                importJunitLines.add(IntObjectPair.of(lineIdx, line));
            }
        }
        if (importJunitLines.isEmpty()) {
            return sourceLines;
        }

        var junitImportPair = IntObjectPair.<String>of(-1, junitDisplayNameImport);
        var sortedJunitImports = Stream.<IntObjectPair<String>>concat(
                        importJunitLines.stream(), Stream.of(junitImportPair))
                .sorted(Comparator.comparing(IntObjectPair::right))
                .collect(Collectors.toList());
        var indexOfJunitDisplayNameImport = sortedJunitImports.indexOf(junitImportPair);
        if (indexOfJunitDisplayNameImport == 0) {
            var idx = sortedJunitImports.get(1).left();
            return sourceLines.pushAbove(idx, junitDisplayNameImport);
        } else {
            var idx = sortedJunitImports.get(indexOfJunitDisplayNameImport - 1).left();
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

    private Stream<Transformation> displayNameToMethodNameTransformations(EnforcementMeta.Item item) {
        var candidate = item.candidate();
        var newName = candidate.newName();
        var originalName = candidate.originalName();

        if (newName.isEmpty() || originalName.equals(newName) || candidate.kind() != EnforceCandidate.Kind.METHOD) {
            return Stream.<Transformation>empty();
        }
        var sourceFile = item.sourceFile();
        var lines = sourceFile.lines();
        var linesView = lines.view();
        var methodNameMatches = new ArrayList<MethodNameMatch>(2);

        for (int lineIdx = 0, linesSize = linesView.size(); lineIdx < linesSize; lineIdx++) {
            var line = linesView.get(lineIdx);
            var matchIndexes = new StringMatch(line, originalName).matchingIndexes();
            if (matchIndexes.notEmpty()) {
                methodNameMatches.add(new MethodNameMatch(lineIdx, matchIndexes));
            }
        }
        if (methodNameMatches.isEmpty()) {
            return Stream.<Transformation>empty();
        }
        if (methodNameMatches.size() == 1) {
            var methodNameMatch = methodNameMatches.get(0);
            var matchIndexes = methodNameMatch.matchIndexes();
            if (matchIndexes.size() == 1) {
                return Stream.<Transformation>of(Transformation.of(
                        lines,
                        item,
                        (sl) -> sl.replaceAt(
                                methodNameMatch.lineIndex(), line -> line.replace(originalName, newName))));
            }
        } else {
            var finalIndexes = new IntVector(methodNameMatches.size());
            methodNameMatches.forEach(match -> {
                var line = linesView.get(match.lineIndex());
                if (line.contains("void " + originalName + "(")) {
                    finalIndexes.add(match.lineIndex());
                }
            });
            if (finalIndexes.size() == 1) {
                return Stream.<Transformation>of(Transformation.of(
                        lines,
                        item,
                        (sl) -> sl.replaceAt(
                                finalIndexes.get(0),
                                line -> line.replace("void " + originalName + "(", "void " + newName + "("))));
            } else {
                var maybeIndexOfClosestClassDistance = findIndexOfClosestClassDistance(item, linesView, finalIndexes);
                if (maybeIndexOfClosestClassDistance.isPresent()) {
                    var indexOfClosestClassDistance = maybeIndexOfClosestClassDistance.get();
                    return Stream.<Transformation>of(Transformation.of(
                            lines,
                            item,
                            (sl) -> sl.replaceAt(
                                    indexOfClosestClassDistance, line -> line.replace(originalName, newName))));
                }
            }
        }
        return Stream.<Transformation>empty();
    }

    private static final class MethodNameMatch {

        private final int lineIndex;
        private final ImmutableIntVector matchIndexes;

        private MethodNameMatch(int lineIndex, ImmutableIntVector matchIndexes) {
            this.lineIndex = lineIndex;
            this.matchIndexes = matchIndexes;
        }

        public int lineIndex() {
            return lineIndex;
        }

        public ImmutableIntVector matchIndexes() {
            return matchIndexes;
        }
    }

    private Optional<Integer> findIndexOfClosestClassDistance(
            EnforcementMeta.Item item, List<String> lines, ImmutableIntVector matchedLineIndexes) {
        var lineIndexToOuterClassDistance = new TreeMap<Integer, Integer>();
        var immediateClassName = item.immediateClassName();
        var targetClassChunk = "class " + immediateClassName + " {";
        var targetInterfaceChunk = "interface " + immediateClassName + " {";
        matchedLineIndexes.forEach(matchedLineIndex -> {
            int distance = 0;
            for (int idx = matchedLineIndex; idx >= 0; idx--) {
                var line = lines.get(idx);
                if (line.contains(immediateClassName)
                        && (line.contains(targetClassChunk) || line.contains(targetInterfaceChunk))) {
                    lineIndexToOuterClassDistance.put(matchedLineIndex, distance);
                    break;
                }
                distance++;
            }
        });
        return Optional.ofNullable(lineIndexToOuterClassDistance.firstEntry()).map(Map.Entry::getKey);
    }
}
