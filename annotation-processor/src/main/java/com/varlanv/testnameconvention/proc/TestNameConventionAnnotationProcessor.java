package com.varlanv.testnameconvention.proc;

import com.varlanv.testnameconvention.info.EnforcementMeta;
import com.varlanv.testnameconvention.info.XmlEnforceMeta;
import com.varlanv.testnameconvention.info.XmlMemoryEnforceMeta;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.StandardLocation;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.BiFunction;

public class TestNameConventionAnnotationProcessor extends AbstractProcessor {

    private String toMethodNameFromDisplayName(String displayName) {
        // 1. Replace non-alphanumeric characters with underscores
        var methodName = displayName.replaceAll("[^a-zA-Z0-9]+", "_");

        // 2. Remove leading and trailing underscores
        methodName = methodName.replaceAll("^_|_$", "");

        // 3. Convert to lowercase
        methodName = methodName.toLowerCase();

        // 4. Replace multiple consecutive underscores with a single underscore
        methodName = methodName.replaceAll("__+", "_");


        // 5. Handle empty string cases (if the input string contains only special characters)
        if (methodName.isEmpty()) {
            methodName = "default_method_name"; // Or throw an exception, or return null, as you see fit.
        }

        //6. Remove numbers from the start of the string if any
        methodName = methodName.replaceAll("^\\d+", "");

        //7. Replace again multiple consecutive underscores with a single underscore after number removal
        methodName = methodName.replaceAll("__+", "_");

        //8. Remove leading and trailing underscores once more in case numbers were at the start
        methodName = methodName.replaceAll("^_|_$", "");
        return methodName;
    }

    private final Map<String, BiFunction<RoundEnvironment, TypeElement, List<EnforcementMeta.Item>>> strategy = Map.of(
            "org.junit.jupiter.api.DisplayName", (roundEnv, annotation) -> {
                var elements = roundEnv.getElementsAnnotatedWith(annotation);
                var output = new ArrayList<EnforcementMeta.Item>();
                for (var element : elements) {
                    var displayNameValue = element.getAnnotation(DisplayName.class).value();
                    var kind = element.getKind();
                    if (kind == ElementKind.METHOD) {
                        var methodElement = (ExecutableElement) element;
                        var methodName = methodElement.getSimpleName().toString();
                        output.add(
                                new EnforcementMeta.Item(
                                        findTopLevelClassName(element),
                                        displayNameValue,
                                        element.getEnclosingElement().getSimpleName().toString(),
                                        methodName
                                )
                        );
                    } else {
                        output.add(
                                new EnforcementMeta.Item(
                                        findTopLevelClassName(element),
                                        displayNameValue,
                                        ((TypeElement) element).getQualifiedName().toString(),
                                        ""
                                )
                        );
                    }
                }
                return output;
            }
    );

    private String findTopLevelClassName(Element start) {
        String topLevelClassName;
        var enclosingElement = start;
        while (true) {
            var nestedEnclElement = enclosingElement.getEnclosingElement();
            if (nestedEnclElement.getKind() == ElementKind.PACKAGE) {
                topLevelClassName = ((TypeElement) enclosingElement).getQualifiedName().toString();
                break;
            }
            enclosingElement = nestedEnclElement;
        }
        return topLevelClassName;
    }

    @Override
    @SneakyThrows
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        var output = new ArrayList<EnforcementMeta.Item>();
        for (var annotation : annotations) {
            output.addAll(
                    Optional.ofNullable(strategy.get(annotation.getQualifiedName().toString()))
                            .map(action -> action.apply(roundEnv, annotation))
                            .orElse(List.of())
            );
        }
        var filer = processingEnv.getFiler();
        var path = "com/varlanv/testnameconvention/enforcements.xml";
        var existingResource = Paths.get(filer.getResource(StandardLocation.SOURCE_OUTPUT, "", path).toUri());
        if (Files.exists(existingResource)) {
            try (var is = Files.newInputStream(existingResource)) {
                var existingMeta = new XmlEnforceMeta().items(is);
                output.addAll(existingMeta);
            }
            var xmlMemoryEnforceMeta = new XmlMemoryEnforceMeta(output);
            var stringWriter = new StringWriter();
            xmlMemoryEnforceMeta.writeTo(stringWriter);
            var stringXml = stringWriter.toString();
            try (var writer = Files.newBufferedWriter(existingResource)) {
                writer.write(stringXml);
                writer.flush();
            }
        } else {
            Files.createDirectories(existingResource.getParent());
            Files.createFile(existingResource);
            var xmlMemoryEnforceMeta = new XmlMemoryEnforceMeta(output);
            try (var writer = Files.newBufferedWriter(existingResource)) {
                xmlMemoryEnforceMeta.writeTo(writer);
            }
        }
        return true;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(
                "org.junit.jupiter.api.DisplayName",
                "org.junit.jupiter.api.Test"
        );
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_5;
    }
}
