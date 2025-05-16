package com.varlanv.testkonvence.commontest.sample;

import com.varlanv.testkonvence.commontest.BaseTest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

@Getter
@RequiredArgsConstructor
public class Sample {

    String description;
    List<SampleSources> sources;
    List<BaseTest.ThrowingConsumer<ConsumableSample>> extraAssertions;
    SampleOptions options;

    @SneakyThrows
    public void consume(BaseTest.ThrowingConsumer<ConsumableSample> consumer) {
        var dir = Files.createTempDirectory("test-konvence-sample");
        var test = new ArrayList<SampleSourceFile>(this.sources.size());
        for (var source : sources) {
            test.add(toFileSample(source, dir));
        }
        try {
            var consumableSample = new ConsumableSample(description, dir, test, options);
            consumer.accept(consumableSample);
            for (var extraAssertion : extraAssertions) {
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
    }

    @SneakyThrows
    private SampleSourceFile toFileSample(SampleSources sampleSources, Path dir) {
        var resultDir = dir;
        for (var packagePart : sampleSources.packageName().split("\\.")) {
            resultDir = resultDir.resolve(packagePart);
        }
        Files.createDirectories(resultDir);
        var resultFile = Files.writeString(
                resultDir.resolve(sampleSources.fileName()),
                sampleSources.sources(),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE_NEW);
        return new SampleSourceFile(
                resultFile,
                sampleSources.outerClassName(),
                sampleSources.packageName(),
                sampleSources.expectedTransformation());
    }
}
