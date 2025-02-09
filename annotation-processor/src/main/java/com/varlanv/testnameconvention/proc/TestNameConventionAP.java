package com.varlanv.testnameconvention.proc;

import com.varlanv.testnameconvention.info.EnforcementMeta;
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
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestNameConventionAP extends AbstractProcessor {

    public static final String enforcementsXmlPackage = "com.varlanv.testnameconvention";
    public static final String enforcementsXmlName = "testkonvence_enforcements.xml";
    public static final String indentXmlOption = "com.varlanv.testnameconvention.indentXml";

    private static final BiFunction<RoundEnvironment, TypeElement, List<EnforcementMeta.Item>> testAnnotationFn = (roundEnv, annotation) -> {
        var elements = roundEnv.getElementsAnnotatedWith(annotation);
        var output = new ArrayList<EnforcementMeta.Item>();
        for (var element : elements) {
            var displayNameAn = element.getAnnotation(DisplayName.class);
            var kind = element.getKind();
            if (displayNameAn == null) {
                if (kind == ElementKind.METHOD) {
                    var methodElement = (ExecutableElement) element;
                    var methodName = methodElement.getSimpleName().toString();
                    output.add(
                        new EnforcementMeta.Item(
                            findTopLevelClassName(element),
                            "",
                            element.getEnclosingElement().getSimpleName().toString(),
                            methodName
                        )
                    );
                } else {
                    output.add(
                        new EnforcementMeta.Item(
                            findTopLevelClassName(element),
                            "",
                            ((TypeElement) element).getQualifiedName().toString(),
                            ""
                        )
                    );
                }
            } else {
                var displayNameVal = displayNameAn.value();
                if (kind == ElementKind.METHOD) {
                    var methodElement = (ExecutableElement) element;
                    var methodName = methodElement.getSimpleName().toString();
                    output.add(
                        new EnforcementMeta.Item(
                            findTopLevelClassName(element),
                            displayNameVal,
                            element.getEnclosingElement().getSimpleName().toString(),
                            methodName
                        )
                    );
                } else {
                    output.add(
                        new EnforcementMeta.Item(
                            findTopLevelClassName(element),
                            displayNameVal,
                            ((TypeElement) element).getQualifiedName().toString(),
                            ""
                        )
                    );
                }
            }
        }
        return output;
    };

    private static final BiFunction<RoundEnvironment, TypeElement, List<EnforcementMeta.Item>> displayNameAnnotationFn = (roundEnv, annotation) -> {
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
    };

    private static final Set<String> supportedTestAnnotations = Set.of(
        "org.junit.jupiter.api.Test",
        "org.junit.jupiter.params.ParameterizedTest",
        "org.junit.jupiter.api.TestFactory",
        "org.junit.jupiter.api.RepeatedTest"
    );

    private static final Set<String> supportedAnnotations = Stream.of(
            supportedTestAnnotations,
            Set.of(
                "org.junit.jupiter.api.DisplayName"
            )
        )
        .flatMap(Set::stream)
        .collect(Collectors.toSet());

    private static final Map<String, BiFunction<RoundEnvironment, TypeElement, List<EnforcementMeta.Item>>> strategy = Stream.concat(
            supportedTestAnnotations.stream().map(it -> Map.entry(it, testAnnotationFn)),
            Stream.of(Map.entry("org.junit.jupiter.api.DisplayName", displayNameAnnotationFn))
        )
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    private static String findTopLevelClassName(Element start) {
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

    Set<EnforcementMeta.Item> output = new LinkedHashSet<>();

    @Override
    @SneakyThrows
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            var filer = processingEnv.getFiler();
            var xmlMemoryEnforceMeta = new XmlMemoryEnforceMeta(
                output.stream()
                    .sorted(
                        Comparator.comparing(EnforcementMeta.Item::fullEnclosingClassName)
                            .thenComparing(EnforcementMeta.Item::className)
                            .thenComparing(EnforcementMeta.Item::displayName)
                            .thenComparing(EnforcementMeta.Item::methodName)
                    )
                    .toList()
            );
            var resource = filer.createResource(StandardLocation.SOURCE_OUTPUT, enforcementsXmlPackage, enforcementsXmlName);
            try (var writer = resource.openWriter()) {
                if (output.isEmpty()) {
                    writer.write("");
                    writer.flush();
                } else {
                    Optional.ofNullable(processingEnv.getOptions().get(indentXmlOption))
                        .filter("true"::equals)
                        .ifPresentOrElse(
                            ignore -> xmlMemoryEnforceMeta.indentWriteTo(writer),
                            () -> xmlMemoryEnforceMeta.writeTo(writer));
                }
            }
        } else {
            for (var annotation : annotations) {
                output.addAll(
                    Optional.ofNullable(strategy.get(annotation.getQualifiedName().toString()))
                        .map(action -> action.apply(roundEnv, annotation))
                        .orElse(List.of())
                );
            }
        }
        return true;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return supportedAnnotations;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_5;
    }

    @Override
    public Set<String> getSupportedOptions() {
        return Set.of(indentXmlOption);
    }
}
