package com.varlanv.testkonvence.gradle.plugin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

import com.varlanv.testkonvence.Constants;
import com.varlanv.testkonvence.XmlMemoryEnforceMeta;
import com.varlanv.testkonvence.commontest.IntegrationTest;
import com.varlanv.testkonvence.commontest.TestSamples;
import com.varlanv.testkonvence.commontest.sample.ConsumableSample;
import com.varlanv.testkonvence.commontest.sample.SampleSourceFile;
import com.varlanv.testkonvence.proc.TestKonvenceAP;
import io.toolisticon.cute.Cute;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.tools.StandardLocation;
import org.assertj.core.api.ThrowableAssert;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.opentest4j.AssertionFailedError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessorWithEnforcerIntegrationTest implements IntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(ProcessorWithEnforcerIntegrationTest.class);

    @TestFactory
    Stream<DynamicTest> fromSamples() {
        return TestSamples.testSamples().stream()
                .map(sample -> DynamicTest.dynamicTest(sample.description(), () -> sample.consume(this::spec)));
    }

    void spec(ConsumableSample sample) throws Exception {
        var sources = sample.sources().value().stream()
                .collect(Collectors.toMap(SampleSourceFile::outerClassName, SampleSourceFile::content));
        var resultXml = runAnnotationProcessor(sample, sources);
        consumeTempFile(resultXmlPath -> {
            Files.write(resultXmlPath, resultXml);
            new Train(
                            log,
                            resultXmlPath,
                            sample.dir(),
                            ImmutableTrainOptions.builder()
                                    .reverseTransformation(sample.options().reverseTransformation())
                                    .build())
                    .run();

            for (var source : sample.sources()) {
                var actualReader = new BufferedReader(new StringReader(source.content()));
                var expectedReader = new BufferedReader(new StringReader(source.expectedTransformation()));
                var lineCount = 0;
                String actualLine;
                try {
                    while ((actualLine = actualReader.readLine()) != null) {
                        var expectedLine = expectedReader.readLine();
                        assertThat(actualLine).as("Line number [%d]", lineCount).isEqualTo(expectedLine);
                        lineCount++;
                    }
                } catch (AssertionFailedError e) {
                    throw new AssertionFailedError("Found diff", source.expectedTransformation(), source.content(), e);
                }
                assertThat(source.content()).isEqualToIgnoringWhitespace(source.expectedTransformation());
                assertThat(source.content()).isEqualTo(source.expectedTransformation());
            }
        });
    }

    byte[] runAnnotationProcessor(ConsumableSample sample, String className, @Language("Java") String sources)
            throws Exception {
        return runAnnotationProcessor(sample, Map.of(className, sources));
    }

    byte[] runAnnotationProcessor(ConsumableSample sample, Map<String, String> sources) throws Exception {
        var iterator = sources.entrySet().iterator();
        var first = iterator.next();
        var cute = Cute.blackBoxTest()
                .given()
                .processor(TestKonvenceAP.class)
                .andSourceFile(first.getKey(), first.getValue());
        while (iterator.hasNext()) {
            var next = iterator.next();
            cute = cute.andSourceFile(next.getKey(), next.getValue());
        }
        var resultXml = new AtomicReference<byte[]>();
        cute.andUseCompilerOptions(resolveCompilerOptions(sample))
                .whenCompiled()
                .thenExpectThat()
                .compilationSucceeds()
                .andThat()
                .fileObject(
                        StandardLocation.SOURCE_OUTPUT,
                        Constants.apEnforcementsXmlPackage,
                        Constants.apEnforcementsXmlName)
                .exists()
                .executeTest()
                .executeCustomAssertions(outcome -> {
                    var fileManager = outcome.getFileManager();
                    var fileObjects = fileManager.getFileObjects();
                    assertThat(fileObjects).hasSize(1);
                    resultXml.set(fileObjects.get(0).getContentAsByteArray());
                });
        if (resultXml.get() == null) {
            throw new AssertionError("Result xml is null");
        }
        if (resultXml.get().length > 0) {
            ThrowableAssert.ThrowingCallable throwingCallable =
                    () -> XmlMemoryEnforceMeta.fromXmlStream(new ByteArrayInputStream(resultXml.get()));
            assertThatNoException().isThrownBy(throwingCallable);
        }
        return resultXml.get();
    }

    private String[] resolveCompilerOptions(ConsumableSample sample) {
        var args = new ArrayList<>(List.of(
                "-A" + Constants.apIndentXmlOption + "=false", "-A" + Constants.performanceLogProperty + "=true"));
        if (sample.options().reverseTransformation()) {
            args.add("-A" + Constants.apReversedOption + "=true");
        }
        if (sample.options().camelMethodName()) {
            args.add("-A" + Constants.apUseCamelCaseMethodNamesOption + "=true");
        }
        return args.toArray(new String[0]);
    }
}
