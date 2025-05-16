package com.varlanv.testkonvence.gradle.plugin;

import com.varlanv.testkonvence.commontest.IntegrationTest;
import com.varlanv.testkonvence.commontest.TestSamples;
import com.varlanv.testkonvence.commontest.sample.ConsumableSample;
import com.varlanv.testkonvence.commontest.sample.SampleSourceFile;
import com.varlanv.testkonvence.proc.TestKonvenceAP;
import io.toolisticon.cute.Cute;
import lombok.SneakyThrows;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import javax.tools.StandardLocation;
import java.io.BufferedReader;
import java.io.StringReader;
import java.nio.file.Files;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class ProcessorWithEnforcerIntegrationTest implements IntegrationTest {

    @TestFactory
    Stream<DynamicTest> fromSamples() {
        return TestSamples.testSamples().stream()
            .map(sample -> DynamicTest.dynamicTest(
                    sample.description(),
                    () -> sample.consume(this::spec)
                )
            );
    }

    @SneakyThrows
    void spec(ConsumableSample sample) {
        var sources = sample.sources().stream()
            .collect(
                Collectors.toMap(
                    SampleSourceFile::outerClassName,
                    SampleSourceFile::content
                )
            );
        var resultXml = runAnnotationProcessor(sources);
        consumeTempFile(resultXmlPath -> {
            Files.write(resultXmlPath, resultXml);
            new Train(
                resultXmlPath,
                sample.dir(),
                new TrainOptions(false, sample.options().reverseTransformation(), sample.options().camelMethodName())
            ).run();

            for (var source : sample.sources()) {
                var actualReader = new BufferedReader(new StringReader(source.content()));
                var expectedReader = new BufferedReader(new StringReader(source.expectedTransformation()));
                var lineCount = 0;
                String actualLine;
                while ((actualLine = actualReader.readLine()) != null) {
                    var expectedLine = expectedReader.readLine();
                    assertThat(actualLine).as("Line number [%d]", lineCount).isEqualTo(expectedLine);
                    lineCount++;
                }
                assertThat(source.content()).isEqualToIgnoringWhitespace(source.expectedTransformation());
                assertThat(source.content()).isEqualTo(source.expectedTransformation());
            }
        });
    }

    byte[] runAnnotationProcessor(String className, @Language("Java") String sources) {
        return runAnnotationProcessor(Map.of(className, sources));
    }

    byte[] runAnnotationProcessor(Map<String, String> sources) {
        var iterator = sources.entrySet().iterator();
        var first = iterator.next();
        var cute = Cute
            .blackBoxTest()
            .given()
            .processor(TestKonvenceAP.class)
            .andSourceFile(first.getKey(), first.getValue());
        while (iterator.hasNext()) {
            var next = iterator.next();
            cute = cute.andSourceFile(next.getKey(), next.getValue());
        }
        var resultXml = new AtomicReference<byte[]>();
        cute.andUseCompilerOptions("-A" + TestKonvenceAP.indentXmlOption + "=false").whenCompiled()
            .thenExpectThat().compilationSucceeds()
            .andThat().fileObject(StandardLocation.SOURCE_OUTPUT, TestKonvenceAP.enforcementsXmlPackage, TestKonvenceAP.enforcementsXmlName).exists()
            .executeTest()
            .executeCustomAssertions(outcome -> {
                var fileManager = outcome.getFileManager();
                var fileObjects = fileManager.getFileObjects();
                assertThat(fileObjects).hasSize(1);
                resultXml.set(fileObjects.get(0).getContentAsByteArray());
            });
        assertThat(resultXml.get()).isNotEmpty();
        return resultXml.get();
    }
}
