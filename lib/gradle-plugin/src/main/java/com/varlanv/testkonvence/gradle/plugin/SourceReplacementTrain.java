package com.varlanv.testkonvence.gradle.plugin;

import com.varlanv.testkonvence.Constants;
import com.varlanv.testkonvence.FunctionalUtil;
import com.varlanv.testkonvence.TrustedException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

final class SourceReplacementTrain {

    private final Logger log;
    private final TrainOptions trainOptions;
    private final EnforcementMeta enforcementMeta;
    private final TrainPerformanceLog performanceLog;

    SourceReplacementTrain(
            Logger log,
            TrainOptions trainOptions,
            EnforcementMeta enforcementMeta,
            TrainPerformanceLog performanceLog) {
        this.log = log;
        this.trainOptions = trainOptions;
        this.enforcementMeta = enforcementMeta;
        this.performanceLog = performanceLog;
    }

    public void run() throws Exception {
        transformations().consumeGroupedByFile((target, transformations) -> {
            var targetPath = target.path().toAbsolutePath();
            var transformationsSorted = new TreeSet<IntObjectPair<Transformation>>((a, b) -> {
                if (a.right()
                        .input()
                        .meta()
                        .candidate
                        .newName()
                        .equals(b.right().input().meta().candidate.originalName())) {
                    return 1;
                } else {
                    return Integer.compare(a.left(), b.left());
                }
            });
            for (var transformationIdx = 0; transformationIdx < transformations.size(); transformationIdx++) {
                transformationsSorted.add(IntObjectPair.of(transformationIdx, transformations.get(transformationIdx)));
            }
            var resultLines = target.lines();
            for (var transformation : transformationsSorted) {
                var resFinal = resultLines;
                resultLines = performanceLog.printIntermediateSupplier(
                        () -> "Apply transformation to file " + targetPath,
                        () -> transformation.right().action().apply(resFinal));
            }
            var resultLinesFinal = resultLines;
            if (resultLines.changed()) {
                if (trainOptions.dryWithFailing()) {
                    throw new TrustedException(String.format(
                            "[%s] - found test naming mismatch in file [%s]. "
                                    + "Consider running `%s` "
                                    + "Gradle task to apply test naming transformations.",
                            Constants.PLUGIN_NAME,
                            Constants.TEST_KONVENCE_APPLY_TASK_NAME,
                            target.path().toAbsolutePath()));
                } else {
                    try {
                        performanceLog.printIntermediateRunnable(
                                () -> "Saving results at path: " + targetPath, () -> target.save(resultLinesFinal));
                    } catch (Exception e) {
                        log.debug("Failed to apply transformation to file [{}], ignoring exception", targetPath);
                    }
                }
            }
        });
    }

    private Transformations transformations() {
        return enforcementMeta.items().stream()
                .flatMap(item -> Stream.concat(
                        displayNameToMethodNameTransformations(item), methodNameToDisplayNameTransformations(item)))
                .reduce(Transformations.empty(), Transformations::register, FunctionalUtil.throwingCombiner());
    }

    private Stream<Transformation> methodNameToDisplayNameTransformations(EnforcementMeta.Item item) {
        var candidate = item.candidate;
        if (!trainOptions.reverseTransformation() || !candidate.newName().isEmpty()) {
            return Stream.empty();
        }
        return Stream.of(Transformation.of(item.sourceFile.lines(), item, sourceLines -> {
            var linesView = sourceLines.view();
            var displayName = candidate.displayName();
            var softMatchStr = " " + candidate.originalName() + "(";
            var perfectMatchStr = "void " + candidate.originalName() + "(";
            var matchedLineIndexes = new IntVector(3);
            var perfectMatchesIndexes = new IntVector(3);
            var linesSize = linesView.size();
            for (int lineIdx = 0; lineIdx < linesSize; lineIdx++) {
                var line = linesView.get(lineIdx);
                if (line.contains(softMatchStr)) {
                    matchedLineIndexes.add(lineIdx);
                    if (line.contains(perfectMatchStr)) {
                        perfectMatchesIndexes.add(lineIdx);
                    }
                }
            }
            if (matchedLineIndexes.size() == 0) {
                return sourceLines;
            } else if (matchedLineIndexes.size() == 1 || perfectMatchesIndexes.size() == 1) {
                var lineIdx =
                        perfectMatchesIndexes.size() == 1 ? perfectMatchesIndexes.get(0) : matchedLineIndexes.get(0);
                var line = linesView.get(lineIdx);
                var stringBuilder = new StringBuilder();
                var indent = indexOfFirstNonWhiteSpace(line);
                stringBuilder.append(" ".repeat(indent));
                var emptyLineIdx = findIndexOfEmptyLine(lineIdx, linesView);
                if (emptyLineIdx != null) {
                    var displayNameAnnotation = stringBuilder
                            .append("@DisplayName(\"")
                            .append(displayName)
                            .append("\")")
                            .toString();
                    var sl = sourceLines.pushAbove(lineIdx, displayNameAnnotation);
                    return handleDisplayNameImport(sl, item);
                } else {
                    return sourceLines;
                }
            } else {
                var indexes = perfectMatchesIndexes.size() > 0 ? perfectMatchesIndexes : matchedLineIndexes;
                var closesClassLineNum = findLineNumWithClosestDistanceToClass(item, linesView, indexes);
                if (closesClassLineNum != null) {
                    var line = linesView.get(closesClassLineNum);
                    var indent = indexOfFirstNonWhiteSpace(line);
                    var displayNameAnnotation = " ".repeat(indent) + "@DisplayName(\"" + displayName + "\")";
                    var sl = sourceLines.pushAbove(closesClassLineNum, displayNameAnnotation);
                    return handleDisplayNameImport(sl, item);
                }
            }
            return sourceLines;
        }));
    }

    private SourceLines handleDisplayNameImport(SourceLines sourceLines, EnforcementMeta.Item item) {
        var linesView = sourceLines.view();
        var importJunitLines = new LinkedHashSet<IntObjectPair<String>>();
        var junitDisplayNameImport = "import org.junit.jupiter.api.DisplayName;";
        for (int lineIdx = 0; lineIdx < linesView.size(); lineIdx++) {
            var line = linesView.get(lineIdx);
            if (line.contains("class ") && line.contains("{") && line.contains(item.topClassSimpleName)) {
                break;
            }
            if (line.contains("import org.junit.jupiter.api.*") || line.contains(junitDisplayNameImport)) {
                return sourceLines;
            } else if (line.contains("import org.junit.jupiter.api")
                    || line.contains("org.junit.jupiter.params.ParameterizedTest")) {
                importJunitLines.add(IntObjectPair.of(lineIdx, line));
            }
        }
        if (importJunitLines.isEmpty()) {
            return sourceLines;
        }

        var junitImportPair = IntObjectPair.of(-1, junitDisplayNameImport);
        var sortedJunitImports = Stream.concat(importJunitLines.stream(), Stream.of(junitImportPair))
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

    @Nullable private Integer findIndexOfEmptyLine(int start, List<String> lines) {
        for (int i = start; i > 0; i--) {
            if (lines.get(i).trim().isEmpty()) {
                return i;
            }
        }
        return null;
    }

    private Stream<Transformation> displayNameToMethodNameTransformations(EnforcementMeta.Item item) {
        var candidate = item.candidate;
        var newName = candidate.newName();
        var originalName = candidate.originalName();

        if (newName.isEmpty() || originalName.equals(newName)) {
            return Stream.empty();
        }
        var sourceFile = item.sourceFile;
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
            return Stream.empty();
        }
        if (methodNameMatches.size() == 1) {
            var methodNameMatch = methodNameMatches.get(0);
            var matchIndexes = methodNameMatch.matchIndexes();
            if (matchIndexes.size() == 1) {
                return Stream.of(Transformation.of(
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
                return Stream.of(Transformation.of(
                        lines,
                        item,
                        (sl) -> sl.replaceAt(
                                finalIndexes.get(0),
                                line -> line.replace("void " + originalName + "(", "void " + newName + "("))));
            } else {
                var closesClassLineNum = findLineNumWithClosestDistanceToClass(item, linesView, finalIndexes);
                if (closesClassLineNum != null) {
                    return Stream.of(Transformation.of(
                            lines,
                            item,
                            (sl) -> sl.replaceAt(closesClassLineNum, line -> line.replace(originalName, newName))));
                }
            }
        }
        return Stream.empty();
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

    @Nullable private Integer findLineNumWithClosestDistanceToClass(
            EnforcementMeta.Item item, List<String> lines, ImmutableIntVector matchedLineIndexes) {
        var lineIndexToOuterClassDistance = new TreeMap<Integer, Integer>();
        var immediateClassName = item.immediateClassName;
        var targetClassChunk = "class " + immediateClassName + " ";
        var targetInterfaceChunk = "interface " + immediateClassName + " ";
        matchedLineIndexes.forEach(matchedLineIndex -> {
            int distance = 0;
            for (int idx = matchedLineIndex; idx >= 0; idx--) {
                var line = lines.get(idx);
                if (line.contains(targetClassChunk) || line.contains(targetInterfaceChunk)) {
                    lineIndexToOuterClassDistance.put(matchedLineIndex, distance);
                    break;
                }
                distance++;
            }
        });
        var entry = lineIndexToOuterClassDistance.firstEntry();
        if (entry == null) {
            return null;
        }
        return entry.getKey();
    }

    private static int indexOfFirstNonWhiteSpace(String str) {
        var len = str.length();
        for (var i = 0; i < len; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return i;
            }
        }
        return -1;
    }
}
