package com.varlanv.testkonvence.proc;

import com.varlanv.testkonvence.Constants;
import com.varlanv.testkonvence.ImmutableAPEnforcementFull;
import com.varlanv.testkonvence.Pair;
import com.varlanv.testkonvence.XmlMemoryEnforceMeta;
import org.jspecify.annotations.Nullable;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.StandardLocation;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class TestKonvenceAP extends AbstractProcessor {

    public static final Set<String> supportedOptions = Set.of(
        Constants.apIndentXmlOption,
        Constants.apReversedOption,
        Constants.apUseCamelCaseMethodNamesOption
    );
    @Nullable
    private Boolean isCamelCase;
    @Nullable
    private Boolean isReverseEnabled;

    private String convertDisplayNameToMethodName(String displayName) {
        return isCamelCase()
            ? CamelMethodNameFromDisplayName.convert(displayName)
            : SnakeMethodNameFromDisplayName.convert(displayName);
    }

    private boolean isCamelCase() {
        var res = isCamelCase;
        if (res == null) {
            res = processingEnv.getOptions().getOrDefault(Constants.apUseCamelCaseMethodNamesOption, "false").equals("true");
            isCamelCase = res;
        }
        return res;
    }

    private boolean isReversedEnabled() {
        var res = isReverseEnabled;
        if (res == null) {
            res = processingEnv.getOptions().getOrDefault(Constants.apReversedOption, "true").equals("true");
            isReverseEnabled = res;
        }
        return res;
    }

    private final AnnotationStrategy testAnnotationFn =
        (roundEnv, annotation, out) -> {
            var elements = roundEnv.getElementsAnnotatedWith(annotation);
            for (var element : elements) {
                if (element.getKind() == ElementKind.METHOD) {
                    var displayNameValue = findDisplayNameAnnotationValue(element);
                    var className = element.getEnclosingElement()
                        .getSimpleName()
                        .toString();
                    if (displayNameValue == null) {
                        if (isReversedEnabled()) {
                            var methodElement = (ExecutableElement) element;
                            var methodName = methodElement.getSimpleName().toString();
                            out.add(ImmutableAPEnforcementFull.builder()
                                .fullEnclosingClassName(findTopLevelClassName(element))
                                .displayName(DisplayNameFromMethodName.convert(methodName))
                                .className(className)
                                .originalName(methodName)
                                .newName("")
                                .build());
                        }
                    } else {
                        var methodElement = (ExecutableElement) element;
                        var methodName =
                            methodElement.getSimpleName().toString();
                        var newName = convertDisplayNameToMethodName(displayNameValue);
                        if (!Objects.equals(newName, methodName)) {
                            out.add(ImmutableAPEnforcementFull.builder()
                                .fullEnclosingClassName(findTopLevelClassName(element))
                                .displayName(displayNameValue)
                                .className(className)
                                .originalName(methodName)
                                .newName(newName)
                                .build());
                        }
                    }
                }
            }
            return out;
        };


    private final AnnotationStrategy displayNameAnnotationFn =
        (roundEnv, annotation, out) -> {
            var elements = roundEnv.getElementsAnnotatedWith(annotation);
            for (var element : elements) {
                if (element.getKind() == ElementKind.METHOD) {
                    var displayNameValue = findDisplayNameAnnotationValue(element);
                    if (displayNameValue != null) {
                        var methodElement = (ExecutableElement) element;
                        var methodName = methodElement.getSimpleName().toString();
                        var newName = convertDisplayNameToMethodName(displayNameValue);
                        if (!Objects.equals(newName, methodName)) {
                            out.add(ImmutableAPEnforcementFull.builder()
                                .fullEnclosingClassName(findTopLevelClassName(element))
                                .displayName(displayNameValue)
                                .className(element.getEnclosingElement()
                                    .getSimpleName()
                                    .toString())
                                .originalName(methodName)
                                .newName(newName)
                                .build());
                        }
                    }
                }
            }
            return out;
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

    @FunctionalInterface
    private interface AnnotationStrategy {

        APIntermediateOutput apply(RoundEnvironment roundEnvironment, TypeElement typeElement, APIntermediateOutput output);
    }

    private final Map<String, AnnotationStrategy> strategy =
        Stream.concat(
                supportedTestAnnotations.stream()
                    .map(annotationName -> Pair.of(annotationName, testAnnotationFn)),
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

    private void writeResult() throws Exception {
        var filer = processingEnv.getFiler();
        var xmlMemoryEnforceMeta = XmlMemoryEnforceMeta.fromEntriesCollection(output.items());
        var resource =
            filer.createResource(StandardLocation.SOURCE_OUTPUT, Constants.apEnforcementsXmlPackage, Constants.apEnforcementsXmlName);
        try (var writer = resource.openWriter()) {
            if (output.isEmpty()) {
                writer.write("");
                writer.flush();
            } else {
                var xmlOption = processingEnv.getOptions().get(Constants.apIndentXmlOption);
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

    @Nullable
    private static String findDisplayNameAnnotationValue(Element element) {
        for (var annotationMirror : element.getAnnotationMirrors()) {
            var typeElement = (TypeElement) annotationMirror.getAnnotationType().asElement();
            if (typeElement.getQualifiedName().contentEquals("org.junit.jupiter.api.DisplayName")) {
                var elementValues = annotationMirror.getElementValues();
                for (var entry : elementValues.entrySet()) {
                    if (entry.getKey().getSimpleName().contentEquals("value")) {
                        return String.valueOf(entry.getValue().getValue()).trim();
                    }
                }
            }
        }
        return null;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
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
        } else {
            var newOutput = output;
            for (var annotation : annotations) {
                var fn = strategy.get(annotation.getQualifiedName().toString());
                if (fn != null) {
                    newOutput = fn.apply(roundEnv, annotation, newOutput);
                }
            }
            output = newOutput;
        }
        return false;
    }
}
