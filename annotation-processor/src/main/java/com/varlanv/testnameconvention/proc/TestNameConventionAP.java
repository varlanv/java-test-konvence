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

public class TestNameConventionAP extends AbstractProcessor {

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

    BiFunction<RoundEnvironment, TypeElement, List<EnforcementMeta.Item>> testFn = (roundEnv, annotation) -> {
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

    Set<String> supportedAnnotations = Set.of(
        "org.junit.jupiter.api.Test",
        "org.junit.jupiter.params.ParameterizedTest",
        "org.junit.jupiter.api.TestFactory",
        "org.junit.jupiter.api.RepeatedTest",
        "org.junit.jupiter.api.DisplayName"
    );

    Map<String, BiFunction<RoundEnvironment, TypeElement, List<EnforcementMeta.Item>>> strategy = Map.of(
        "org.junit.jupiter.api.Test", testFn,
        "org.junit.jupiter.params.ParameterizedTest", testFn,
        "org.junit.jupiter.api.TestFactory", testFn,
        "org.junit.jupiter.api.RepeatedTest", testFn,
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

    static String enforcementsXmlPackage = "com.varlanv.testnameconvention";
    static String enforcementsXmlName = "enforcements.xml";
    static String indentXmlOption = "com.varlanv.testnameconvention.indentXml";
    Set<EnforcementMeta.Item> output = new LinkedHashSet<>();

    @Override
    @SneakyThrows
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            var filer = processingEnv.getFiler();
            var xmlMemoryEnforceMeta = new XmlMemoryEnforceMeta(
                output.stream()
                    .sorted(Comparator.comparing(EnforcementMeta.Item::fullEnclosingClassName))
                    .toList()
            );
            var resource = filer.createResource(StandardLocation.SOURCE_OUTPUT, enforcementsXmlPackage, enforcementsXmlName);
            try (var writer = resource.openWriter()) {
                if (output.isEmpty()) {
                    writer.write("");
                    writer.flush();
                } else {
                    if (processingEnv.getOptions().containsKey(indentXmlOption)) {
                        xmlMemoryEnforceMeta.indentWriteTo(writer);
                    } else {
                        xmlMemoryEnforceMeta.writeTo(writer);
                    }
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
