package com.varlanv.testkonvence.proc;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.StandardLocation;
import lombok.SneakyThrows;
import lombok.Value;

public class TestKonvenceAP extends AbstractProcessor {

    public static final String enforcementsXmlPackage = "com.varlanv.testkonvence";
    public static final String enforcementsXmlName = "testkonvence_enforcements.xml";
    public static final String indentXmlOption = "com.varlanv.testkonvence.indentXml";

    private final BiFunction<RoundEnvironment, TypeElement, List<APEnforcementMeta.Item>> testAnnotationFn =
            (roundEnv, annotation) -> {
                var elements = roundEnv.getElementsAnnotatedWith(annotation);
                var output = new ArrayList<APEnforcementMeta.Item>();
                for (var element : elements) {
                    var kind = element.getKind();
                    output.add(findDisplayNameAnnotationValue(element)
                            .map(displayNameValue -> {
                                if (kind == ElementKind.METHOD) {
                                    var methodElement = (ExecutableElement) element;
                                    var methodName =
                                            methodElement.getSimpleName().toString();
                                    return ImmutableItem.builder()
                                            .fullEnclosingClassName(findTopLevelClassName(element))
                                            .displayName(displayNameValue)
                                            .className(element.getEnclosingElement()
                                                    .getSimpleName()
                                                    .toString())
                                            .methodName(methodName)
                                            .build();
                                } else {
                                    return ImmutableItem.builder()
                                            .fullEnclosingClassName(findTopLevelClassName(element))
                                            .displayName(displayNameValue)
                                            .className(((TypeElement) element)
                                                    .getQualifiedName()
                                                    .toString())
                                            .methodName("")
                                            .build();
                                }
                            })
                            .orElseGet(() -> {
                                if (kind == ElementKind.METHOD) {
                                    var methodElement = (ExecutableElement) element;
                                    var methodName =
                                            methodElement.getSimpleName().toString();
                                    return ImmutableItem.builder()
                                            .fullEnclosingClassName(findTopLevelClassName(element))
                                            .displayName("")
                                            .className(element.getEnclosingElement()
                                                    .getSimpleName()
                                                    .toString())
                                            .methodName(methodName)
                                            .build();
                                } else {
                                    return ImmutableItem.builder()
                                            .fullEnclosingClassName(findTopLevelClassName(element))
                                            .displayName("")
                                            .className(((TypeElement) element)
                                                    .getQualifiedName()
                                                    .toString())
                                            .methodName("")
                                            .build();
                                }
                            }));
                }
                return output;
            };

    private final BiFunction<RoundEnvironment, TypeElement, List<APEnforcementMeta.Item>> displayNameAnnotationFn =
            (roundEnv, annotation) -> {
                var elements = roundEnv.getElementsAnnotatedWith(annotation);
                var output = new ArrayList<APEnforcementMeta.Item>();
                for (var element : elements) {
                    findDisplayNameAnnotationValue(element).ifPresent(displayNameValue -> {
                        var kind = element.getKind();
                        if (kind == ElementKind.METHOD) {
                            var methodElement = (ExecutableElement) element;
                            var methodName = methodElement.getSimpleName().toString();
                            output.add(ImmutableItem.builder()
                                    .fullEnclosingClassName(findTopLevelClassName(element))
                                    .displayName(displayNameValue)
                                    .className(element.getEnclosingElement()
                                            .getSimpleName()
                                            .toString())
                                    .methodName(methodName)
                                    .build());
                        } else {
                            output.add(ImmutableItem.builder()
                                    .fullEnclosingClassName(findTopLevelClassName(element))
                                    .displayName(displayNameValue)
                                    .className(((TypeElement) element)
                                            .getQualifiedName()
                                            .toString())
                                    .methodName("")
                                    .build());
                        }
                    });
                }
                return output;
            };

    private static final Set<String> supportedTestAnnotations = new HashSet<>(Arrays.asList(
            "org.junit.jupiter.api.Test",
            "org.junit.jupiter.params.ParameterizedTest",
            "org.junit.jupiter.api.TestFactory",
            "org.junit.jupiter.api.RepeatedTest",
            "org.junit.jupiter.api.DynamicTest"));

    private static final Set<String> supportedAnnotations = Stream.of(
                    supportedTestAnnotations, Collections.singleton("org.junit.jupiter.api.DisplayName"))
            .flatMap(Set::stream)
            .collect(Collectors.toSet());

    @Value
    private static class Pair<T1, T2> {
        T1 left;
        T2 right;
    }

    private final Map<String, BiFunction<RoundEnvironment, TypeElement, List<APEnforcementMeta.Item>>> strategy =
            Stream.concat(
                            supportedTestAnnotations.stream().map(it -> new Pair<>(it, testAnnotationFn)),
                            Stream.of(new Pair<>("org.junit.jupiter.api.DisplayName", displayNameAnnotationFn)))
                    .collect(Collectors.toMap(Pair::left, Pair::right));

    private String findTopLevelClassName(Element start) {
        String topLevelClassName;
        Element enclosingElement = start;
        while (true) {
            var nestedEnclElement = enclosingElement.getEnclosingElement();
            if (nestedEnclElement.getKind() == ElementKind.PACKAGE) {
                topLevelClassName =
                        ((TypeElement) enclosingElement).getQualifiedName().toString();
                break;
            }
            enclosingElement = nestedEnclElement;
        }
        return topLevelClassName;
    }

    Set<APEnforcementMeta.Item> output = new LinkedHashSet<>();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            writeResult();
        } else {
            for (var annotation : annotations) {
                Optional.ofNullable(strategy.get(annotation.getQualifiedName().toString()))
                        .map(action -> action.apply(roundEnv, annotation))
                        .ifPresent(output::addAll);
            }
        }
        return false;
    }

    @SneakyThrows
    private void writeResult() {
        var filer = processingEnv.getFiler();
        var xmlMemoryEnforceMeta = new XmlMemoryEnforceMeta(output.stream()
                .sorted(Comparator.comparing(APEnforcementMeta.Item::fullEnclosingClassName)
                        .thenComparing(APEnforcementMeta.Item::className)
                        .thenComparing(APEnforcementMeta.Item::displayName)
                        .thenComparing(APEnforcementMeta.Item::methodName))
                .collect(Collectors.toList()));
        var resource =
                filer.createResource(StandardLocation.SOURCE_OUTPUT, enforcementsXmlPackage, enforcementsXmlName);
        try (var writer = resource.openWriter()) {
            if (output.isEmpty()) {
                writer.write("");
                writer.flush();
            } else {
                var xmlOption = processingEnv.getOptions().get(indentXmlOption);
                if ("true".equals(xmlOption)) {
                    xmlMemoryEnforceMeta.indentWriteTo(writer);
                } else {
                    xmlMemoryEnforceMeta.writeTo(writer);
                }
            }
        }
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return supportedAnnotations;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedOptions() {
        return Collections.singleton(indentXmlOption);
    }

    private Optional<String> findDisplayNameAnnotationValue(Element element) {
        return element.getAnnotationMirrors().stream()
                .flatMap(mirror -> {
                    var typeElement = (TypeElement) mirror.getAnnotationType().asElement();
                    if (!typeElement.getQualifiedName().contentEquals("org.junit.jupiter.api.DisplayName")) {
                        return Stream.empty();
                    }
                    return mirror.getElementValues().entrySet().stream()
                            .filter(entry -> entry.getKey().getSimpleName().contentEquals("value"))
                            .map(entry -> String.valueOf(entry.getValue().getValue()));
                })
                .findAny()
                .map(String::trim);
    }
}
