package com.varlanv.testkonvence.commontest.sample;

import com.varlanv.testkonvence.commontest.BaseTest;
import com.varlanv.testkonvence.commontest.ImmutableList;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;

@Value.Immutable(builder = false)
public interface Sample {

    @Value.Parameter
    String description();

    @Value.Parameter
    ImmutableList<SampleSources> sources();

    @Value.Parameter
    ImmutableList<BaseTest.ThrowingConsumer<ConsumableSample>> extraAssertions();

    @Value.Parameter
    SampleOptions options();

    default void consume(BaseTest.ThrowingConsumer<ConsumableSample> consumer) {
        BaseTest.runQuiet(() -> {
            var dir = Files.createTempDirectory("test-konvence-sample");
            var sources = this.sources().value();
            var test = new ArrayList<SampleSourceFile>(sources.size());
            for (var source : sources) {
                test.add(toFileSample(source, dir));
            }
            try {
                var consumableSample =
                        ImmutableConsumableSample.of(description(), dir, ImmutableList.copyOf(test), options());
                consumer.accept(consumableSample);
                for (var extraAssertion : extraAssertions()) {
                    extraAssertion.accept(consumableSample);
                }
            } finally {
                Files.walkFileTree(dir, new SimpleFileVisitor<>() {

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(Path file, @NotNull BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }
                });
            }
        });
    }

    private SampleSourceFile toFileSample(SampleSources sampleSources, Path dir) {
        return BaseTest.supplyQuiet(() -> {
            var resultDir = dir;
            for (var packagePart : sampleSources.packageName().split("\\.", -1)) {
                resultDir = resultDir.resolve(packagePart);
            }
            Files.createDirectories(resultDir);
            var resultFile = Files.writeString(
                    resultDir.resolve(sampleSources.fileName()),
                    sampleSources.sources(),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE_NEW);
            return ImmutableSampleSourceFile.of(
                    resultFile,
                    sampleSources.outerClassName(),
                    sampleSources.packageName(),
                    sampleSources.expectedTransformation());
        });
    }
}
