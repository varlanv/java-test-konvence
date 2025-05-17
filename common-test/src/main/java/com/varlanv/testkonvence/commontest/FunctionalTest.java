package com.varlanv.testkonvence.commontest;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.util.GradleVersion;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.params.provider.Arguments;

@Tags({@Tag(BaseTest.FUNCTIONAL_TEST_TAG), @Tag(BaseTest.SLOW_TEST_TAG)})
public interface FunctionalTest extends BaseTest {

    Set<String> IGNORED_FILES_FOR_COPY = Set.of(".git", ".gradle", ".idea", "build", "out", "target");

    default Path projectRoot() {
        return Objects.requireNonNull(TestUtils.projectRoot());
    }

    @BeforeAll
    default void setupFunctionalSpec() {
        TestUtils.setProjectRoot(() -> findDirContaining(
                file -> "internal-convention-plugin".equals(file.getFileName().toString())));
    }

    default void runFunctionalFixture(ThrowingConsumer<FunctionalFixture> fixtureConsumer) {
        useTempDir(rootTestProjectDir -> {
            var subjectProjectDir = Files.createDirectory(rootTestProjectDir.resolve("test-subject-project"));
            var settingsFile = subjectProjectDir.resolve("settings.gradle");
            var rootBuildFile = subjectProjectDir.resolve("build.gradle");
            var propertiesFile = subjectProjectDir.resolve("gradle.properties");
            fixtureConsumer.accept(new FunctionalFixture(
                    rootTestProjectDir, subjectProjectDir, settingsFile, rootBuildFile, propertiesFile));
        });
    }

    default void runGradleRunnerFixture(
            DataTable params, String taskName, ThrowingConsumer<RunnerFunctionalFixture> fixtureConsumer) {
        runGradleRunnerFixture(params, List.of(taskName), fixtureConsumer);
    }

    default void runGradleRunnerFixture(
            DataTable params, List<String> arguments, ThrowingConsumer<RunnerFunctionalFixture> fixtureConsumer) {
        runFunctionalFixture(parentFixture -> {
            var args = new ArrayList<>(arguments);
            if (params.configurationCache()) {
                args.add("--configuration-cache");
            }
            if (params.buildCache()) {
                args.add("--build-cache");
            }
            args.add("--warning-mode=summary");
            args.add("-Dorg.gradle.logging.level=lifecycle");
            args.add("-Dorg.gradle.logging.stacktrace=all");
            var env = new HashMap<>(System.getenv());
            env.put("FUNCTIONAL_SPEC_RUN", "true");
            env.putAll(params.isCi() ? Map.of("CI", "true") : Map.of("CI", "false"));
            fixtureConsumer.accept(new RunnerFunctionalFixture(
                    GradleRunner.create()
                            .withPluginClasspath()
                            .withProjectDir(parentFixture.subjectProjectDir.toFile())
                            .withEnvironment(env)
                            .withArguments(args)
                            .forwardOutput()
                            .withGradleVersion(params.gradleVersion()),
                    parentFixture.rootTestProjectDir,
                    parentFixture.subjectProjectDir,
                    parentFixture.settingsFile,
                    parentFixture.rootBuildFile,
                    parentFixture.propertiesFile));
        });
    }

    @SneakyThrows
    default BuildResult build(GradleRunner runner, ThrowingFunction<GradleRunner, BuildResult> runFn) {
        var lineStart = "*".repeat(215);
        var lineEnd = "*".repeat(161);
        var mark = "*".repeat(40);
        System.err.println();
        System.err.println(lineStart);
        System.err.println();
        System.err.printf(
                "%s STARTING GRADLE FUNCTIONAL TEST BUILD FOR SPEC %s. "
                        + "LOGS BELOW ARE COMMING FROM GRADLE BUILD UNDER TEST %s%n",
                mark, getClass().getSimpleName(), mark);
        System.err.printf("Gradle build args: [%s]%n", String.join(" ", runner.getArguments()));
        System.err.printf("Java version - [%s]%n", System.getProperty("java.version"));
        System.err.println();
        System.err.println(lineStart);
        System.err.println();
        try {
            return runFn.apply(runner);
        } finally {
            System.err.println();
            System.err.println(lineEnd);
            System.err.println();
            System.err.printf(
                    "%s FINISHED GRADLE FUNCTIONAL TEST BUILD FOR %s %s%n",
                    mark, getClass().getSimpleName(), mark);
            System.err.println();
            System.err.println(lineEnd);
            System.err.println();
        }
    }

    default void verifyConfigurationCacheNotStored(BuildResult buildResult, String gradleVersion) {
        if (GradleVersion.version(gradleVersion).compareTo(GradleVersion.version("8.0")) > 0) {
            assertThat(buildResult.getOutput())
                    .contains("Configuration cache entry discarded because incompatible task was found:");
        } else {
            assertThat(buildResult.getOutput())
                    .contains("Calculating task graph as no configuration cache is available for tasks:");
        }
    }

    default void copyFolderContents(String srcDirPath, String destDirPath) {
        copyFolderContents(Paths.get(srcDirPath), Paths.get(destDirPath));
    }

    @SneakyThrows
    default void copyFolderContents(Path srcDir, Path destDir) {
        if (Files.notExists(srcDir)) {
            throw new IllegalArgumentException(
                    "Cannot copy from non-existing directory '%s'!".formatted(srcDir.toAbsolutePath()));
        }
        if (!Files.isDirectory(srcDir)) {
            throw new IllegalArgumentException(
                    "Cannot copy from non-directory '%s'!".formatted(srcDir.toAbsolutePath()));
        }

        Files.createDirectories(destDir);
        try (var stream = Files.walk(srcDir)) {
            stream.forEach(path -> {
                var relative = srcDir.relativize(path);
                var newPath = destDir.resolve(relative);
                try {
                    if (Files.isDirectory(path)) {
                        Files.createDirectories(newPath);
                    } else {
                        Files.copy(path, newPath);
                    }
                } catch (Exception e) {
                    BaseTest.hide(e);
                }
            });
        }
        try (var stream = Files.walk(destDir)) {
            stream.forEach(path -> {
                var fileName = path.getFileName().toString();
                if (IGNORED_FILES_FOR_COPY.contains(fileName) && Files.isDirectory(path)) {
                    try {
                        FileUtils.deleteDirectory(path.toFile());
                    } catch (IOException e) {
                        BaseTest.hide(e);
                    }
                }
            });
        }
    }

    default Path useCasesDir() {
        return findDirContaining(file -> "use-cases".equals(file.getFileName().toString()));
    }

    default Path findDirContaining(ThrowingPredicate<Path> predicate) {
        return findDir(file -> {
            try (Stream<Path> stream = Files.list(file)) {
                return stream.anyMatch(predicate.toUnnchecked());
            }
        });
    }

    default Path findDir(String dirName) {
        return findDir(file -> file.getFileName().toString().equals(dirName));
    }

    @SneakyThrows
    default Path findDir(ThrowingPredicate<Path> predicate) {
        return findDir(predicate, new File("").getCanonicalFile().toPath());
    }

    @SneakyThrows
    default Path findDir(ThrowingPredicate<Path> predicate, Path current) {
        if (predicate.test(current)) {
            return current;
        } else {
            var parentFile = current.getParent();
            if (parentFile == null) {
                throw new RuntimeException("Cannot find directory with predicate");
            }
            return findDir(predicate, parentFile);
        }
    }

    default void printFileTree(Path path) {
        System.out.println(
                FileUtils.listFiles(
                                path.toFile(),
                                TrueFileFilter.INSTANCE,
                                new NotFileFilter(new NameFileFilter(".gradle")))
                        .stream()
                        .map(it -> it.getAbsolutePath() + System.lineSeparator())
                        .collect(Collectors.joining()));
    }

    default Stream<Arguments> defaultDataTables() {
        return DataTables.streamDefault().map(Arguments::of);
    }

    record FunctionalFixture(
            Path rootTestProjectDir,
            Path subjectProjectDir,
            Path settingsFile,
            Path rootBuildFile,
            Path propertiesFile) {}

    record RunnerFunctionalFixture(
            GradleRunner runner,
            Path rootTestProjectDir,
            Path subjectProjectDir,
            Path settingsFile,
            Path rootBuildFile,
            Path propertiesFile) {}
}
