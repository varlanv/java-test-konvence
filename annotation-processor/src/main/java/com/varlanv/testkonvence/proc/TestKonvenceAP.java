package com.varlanv.testkonvence.proc;

import com.varlanv.testkonvence.APEnforcementFull;
import com.varlanv.testkonvence.Constants;
import com.varlanv.testkonvence.ImmutableAPEnforcementFull;
import com.varlanv.testkonvence.Pair;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.tools.Diagnostic;
import javax.tools.StandardLocation;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class TestKonvenceAP extends AbstractProcessor {

    public static final String enforcementsXmlPackage = "com.varlanv.testkonvence";
    public static final String enforcementsXmlName = "testkonvence_enforcements.xml";
    public static final String indentXmlOption = "com.varlanv.testkonvence.indentXml";
    public static final String useCamelCaseMethodNamesOption = "com.varlanv.testkonvence.camelCaseMethods";
    public static final String reversedOption = "com.varlanv.testkonvence.reversed";
    public static final Set<String> supportedOptions = Set.of(indentXmlOption, useCamelCaseMethodNamesOption, reversedOption);

    private String convertDisplayNameToMethodName(String displayName) {
        return isCamelCase()
            ? CamelMethodNameFromDisplayName.convert(displayName)
            : SnakeMethodNameFromDisplayName.convert(displayName);
    }

    private boolean isCamelCase() {
        return processingEnv.getOptions().getOrDefault(useCamelCaseMethodNamesOption, "false").equals("true");
    }

    private boolean isReversedEnabled() {
        return processingEnv.getOptions().getOrDefault(reversedOption, "false").equals("true");
    }

    private final BiFunction<RoundEnvironment, TypeElement, List<APEnforcementFull>> testAnnotationFn =
        (roundEnv, annotation) -> {
            var elements = roundEnv.getElementsAnnotatedWith(annotation);
            var output = new ArrayList<APEnforcementFull>();
            for (var element : elements) {
                var kind = element.getKind();
                findDisplayNameAnnotationValue(element)
                    .ifPresentOrElse(displayNameValue -> {
                        if (kind == ElementKind.METHOD) {
                            var methodElement = (ExecutableElement) element;
                            var methodName =
                                methodElement.getSimpleName().toString();
                            var newName = convertDisplayNameToMethodName(displayNameValue);
                            if (!Objects.equals(newName, methodName)) {
                                ImmutableAPEnforcementFull.builder()
                                    .fullEnclosingClassName(findTopLevelClassName(element))
                                    .displayName(displayNameValue)
                                    .className(element.getEnclosingElement()
                                        .getSimpleName()
                                        .toString())
                                    .methodName(methodName)
                                    .newName(newName)
                                    .reversed(false)
                                    .build();
                            }
                        }
                    }, () -> {
                        if (isReversedEnabled()) {
                            if (kind == ElementKind.METHOD) {
                                var methodElement = (ExecutableElement) element;
                                var methodName =
                                    methodElement.getSimpleName().toString();
                                output.add(ImmutableAPEnforcementFull.builder()
                                    .fullEnclosingClassName(findTopLevelClassName(element))
                                    .displayName(DisplayNameFromMethodName.convert(methodName))
                                    .className(element.getEnclosingElement()
                                        .getSimpleName()
                                        .toString())
                                    .methodName(methodName)
                                    .newName("")
                                    .reversed(true)
                                    .build());
                            }
                        }
                    });
            }
            return output;
        };


    private final BiFunction<RoundEnvironment, TypeElement, List<APEnforcementFull>> displayNameAnnotationFn =
        (roundEnv, annotation) -> {
            var elements = roundEnv.getElementsAnnotatedWith(annotation);
            var output = new ArrayList<APEnforcementFull>();
            for (var element : elements) {
                findDisplayNameAnnotationValue(element).ifPresent(displayNameValue -> {
                    var kind = element.getKind();
                    if (kind == ElementKind.METHOD) {
                        var methodElement = (ExecutableElement) element;
                        var methodName = methodElement.getSimpleName().toString();
                        var newName = convertDisplayNameToMethodName(displayNameValue);
                        if (!Objects.equals(newName, methodName)) {
                            output.add(ImmutableAPEnforcementFull.builder()
                                .fullEnclosingClassName(findTopLevelClassName(element))
                                .displayName(displayNameValue)
                                .className(element.getEnclosingElement()
                                    .getSimpleName()
                                    .toString())
                                .methodName(methodName)
                                .newName(newName)
                                .reversed(false)
                                .build());
                        }
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

    private final Map<String, BiFunction<RoundEnvironment, TypeElement, List<APEnforcementFull>>> strategy =
        Stream.concat(
                supportedTestAnnotations.stream().map(it -> Pair.of(it, testAnnotationFn)),
                Stream.of(Pair.of("org.junit.jupiter.api.DisplayName", displayNameAnnotationFn)))
            .collect(Collectors.toMap(Pair::left, Pair::right));

    private static String findTopLevelClassName(Element start) {
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

    APIntermediateOutput output = new APIntermediateOutput();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            if (!output.isEmpty()) {
                try {
                    writeResult();
                } catch (Exception e) {
                    processingEnv
                        .getMessager()
                        .printMessage(
                            Diagnostic.Kind.WARNING,
                            String.format(
                                "Failed to apply annotation processor for Gradle plugin [%s], "
                                    + "plugin logic will not be applied. Internal error message: %s",
                                Constants.PLUGIN_NAME, e.getMessage()));
                }
            }
        } else {
            for (var annotation : annotations) {
                var fn = strategy.get(annotation.getQualifiedName().toString());
                if (fn != null) {
                    for (var item : fn.apply(roundEnv, annotation)) {
                        output.add(item);
                    }
                }
            }
        }
        return false;
    }

    private void writeResult() throws Exception {
        var filer = processingEnv.getFiler();
        var xmlMemoryEnforceMeta = new XmlMemoryEnforceMeta(output.items());
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
        return supportedOptions;
    }

    private static Optional<String> findDisplayNameAnnotationValue(Element element) {
        return element.getAnnotationMirrors().stream()
            .flatMap(mirror -> {
                var typeElement = (TypeElement) mirror.getAnnotationType().asElement();
                if (!typeElement.getQualifiedName().contentEquals("org.junit.jupiter.api.DisplayName")) {
                    return Stream.<String>empty();
                }
                Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues =
                    mirror.getElementValues();
                return elementValues.entrySet().stream()
                    .filter(entry -> entry.getKey().getSimpleName().contentEquals("value"))
                    .map(entry -> String.valueOf(entry.getValue().getValue()));
            })
            .findAny()
            .map(String::trim);
    }
}
