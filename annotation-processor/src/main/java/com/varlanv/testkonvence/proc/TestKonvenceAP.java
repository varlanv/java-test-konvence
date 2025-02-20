package com.varlanv.testkonvence.proc;

import com.varlanv.testkonvence.info.EnforcementMeta;
import com.varlanv.testkonvence.info.XmlMemoryEnforceMeta;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.val;
import lombok.var;
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

public class TestKonvenceAP extends AbstractProcessor {

    public static final String enforcementsXmlPackage = "com.varlanv.testkonvence";
    public static final String enforcementsXmlName = "testkonvence_enforcements.xml";
    public static final String indentXmlOption = "com.varlanv.testkonvence.indentXml";

    private static final BiFunction<RoundEnvironment, TypeElement, List<EnforcementMeta.Item>> testAnnotationFn = (roundEnv, annotation) -> {
        val elements = roundEnv.getElementsAnnotatedWith(annotation);
        val output = new ArrayList<EnforcementMeta.Item>();
        for (val element : elements) {
            val displayNameAn = element.getAnnotation(DisplayName.class);
            val kind = element.getKind();
            if (displayNameAn == null) {
                if (kind == ElementKind.METHOD) {
                    val methodElement = (ExecutableElement) element;
                    val methodName = methodElement.getSimpleName().toString();
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
                val displayNameVal = displayNameAn.value();
                if (kind == ElementKind.METHOD) {
                    val methodElement = (ExecutableElement) element;
                    val methodName = methodElement.getSimpleName().toString();
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
        val elements = roundEnv.getElementsAnnotatedWith(annotation);
        val output = new ArrayList<EnforcementMeta.Item>();
        for (val element : elements) {
            val displayNameValue = element.getAnnotation(DisplayName.class).value();
            val kind = element.getKind();
            if (kind == ElementKind.METHOD) {
                val methodElement = (ExecutableElement) element;
                val methodName = methodElement.getSimpleName().toString();
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

    private static final Set<String> supportedTestAnnotations = new HashSet<>(
        Arrays.asList(
            "org.junit.jupiter.api.Test",
            "org.junit.jupiter.params.ParameterizedTest",
            "org.junit.jupiter.api.TestFactory",
            "org.junit.jupiter.api.RepeatedTest"
        )
    );

    private static final Set<String> supportedAnnotations = Stream.of(
            supportedTestAnnotations,
            Collections.singleton(
                "org.junit.jupiter.api.DisplayName"
            )
        )
        .flatMap(Set::stream)
        .collect(Collectors.toSet());

    @Value
    private static class Pair<T1, T2> {
        T1 left;
        T2 right;
    }

    private static final Map<String, BiFunction<RoundEnvironment, TypeElement, List<EnforcementMeta.Item>>> strategy = Stream.concat(
            supportedTestAnnotations.stream().map(it -> new Pair<>(it, testAnnotationFn)),
            Stream.of(new Pair<>("org.junit.jupiter.api.DisplayName", displayNameAnnotationFn))
        )
        .collect(Collectors.toMap(Pair::left, Pair::right));

    private static String findTopLevelClassName(Element start) {
        String topLevelClassName;
        var enclosingElement = start;
        while (true) {
            val nestedEnclElement = enclosingElement.getEnclosingElement();
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
            val filer = processingEnv.getFiler();
            val xmlMemoryEnforceMeta = new XmlMemoryEnforceMeta(
                output.stream()
                    .sorted(
                        Comparator.comparing(EnforcementMeta.Item::fullEnclosingClassName)
                            .thenComparing(EnforcementMeta.Item::className)
                            .thenComparing(EnforcementMeta.Item::displayName)
                            .thenComparing(EnforcementMeta.Item::methodName)
                    )
                    .collect(Collectors.toList()));
            val resource = filer.createResource(StandardLocation.SOURCE_OUTPUT, enforcementsXmlPackage, enforcementsXmlName);
            try (val writer = resource.openWriter()) {
                if (output.isEmpty()) {
                    writer.write("");
                    writer.flush();
                } else {
                    val xmlOption = processingEnv.getOptions().get(indentXmlOption);
                    if ("true".equals(xmlOption)) {
                        xmlMemoryEnforceMeta.indentWriteTo(writer);
                    } else {
                        xmlMemoryEnforceMeta.writeTo(writer);
                    }
                }
            }
        } else {
            for (val annotation : annotations) {
                output.addAll(
                    Optional.ofNullable(strategy.get(annotation.getQualifiedName().toString()))
                        .map(action -> action.apply(roundEnv, annotation))
                        .orElse(Collections.emptyList())
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
        return Collections.singleton(indentXmlOption);
    }
}
