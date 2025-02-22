package com.varlanv.testkonvence.gradle.plugin;

import com.varlanv.testkonvence.commontest.GradleIntegrationTest;

public class TestKonvencePluginIntegrationTest implements GradleIntegrationTest {

//    @Test
//    void asd() {
//        runProjectFixture(fixture -> {
//            var project = fixture.project();
//            var annotationProcessorOutputDir = Files.createDirectories(
//                fixture.projectDir()
//                    .resolve("build")
//                    .resolve("generated")
//                    .resolve("sources")
//                    .resolve("annotationProcessor")
//                    .resolve("java")
//                    .resolve("main")
//            );
//            var enforceXml = Files.writeString(
//                annotationProcessorOutputDir.resolve("testkonvence_enforcements.xml"),
//                "",
//                StandardOpenOption.CREATE_NEW
//            );
//            var sourcePackageDir = Files.createDirectories(fixture.projectDir().resolve("src").resolve("main").resolve("java").resolve("testcase"));
//            var testPackageDir = Files.createDirectories(fixture.projectDir().resolve("src").resolve("test").resolve("java").resolve("testcase"));
//            var javaSourceFile = Files.writeString(
//                testPackageDir.resolve("SomeSourceFile.java"),
//                java("""
//                    package testcase;
//
//                    public class SomeSourceFile {}
//                    """
//                ),
//                StandardOpenOption.CREATE_NEW
//            );
//            var javaTestFile = Files.writeString(
//                testPackageDir.resolve("SomeSourceFileTest.java"),
//                java("""
//                    package testcase;
//
//                    class SomeSourceFileTest {
//                        @Test
//                        void someTest() {
//                            System.out.println("something");
//                            System.out.println("something");
//                            System.out.println("something");
//                            System.out.println("something");
//                            System.out.println("something");
//                        }
//                    }
//                    """
//                ),
//                StandardOpenOption.CREATE_NEW
//            );
//            project.getPlugins().apply(JavaPlugin.class);
//            project.getPlugins().apply(TestKonvencePlugin.class);
//            evaluateProject(project);
//            var testTask = project.getTasks().named("test", org.gradle.api.tasks.testing.Test.class).get();
////            List<Action<? super Task>> actions = testTask.getActions();
////            var enforceTask = (TestNameEnforceAction) project.getTasks().getByName(TestNameEnforceAction.name());
//            var javaCompileTask = project.getTasks().named(JavaPlugin.COMPILE_JAVA_TASK_NAME, JavaCompile.class).get();
////
//            System.out.println(
//                FileUtils.listFilesAndDirs(
//                        fixture.projectDir().toFile(),
//                        TrueFileFilter.INSTANCE,
//                        TrueFileFilter.INSTANCE
//                    ).stream()
//                    .map(it -> it.getAbsolutePath() + System.lineSeparator())
//                    .collect(Collectors.joining())
//            );
//            testTask.executeTests();
////            System.out.println(javaCompile.getClasspath().getFiles());
////            System.out.println(enforceTask.getCompileClasspath().getFiles());
////            System.out.println("Java sources root -> " + enforceTask.getSourcesRoot().getAsFile().get().getPath());
////            var resolvedEnforceXml = enforceTask.getEnforceFiles().getSingleFile();
////            System.out.println(resolvedEnforceXml);
////            assertThat(resolvedEnforceXml).exists();
////
////            enforceTask.enforce();
//        });
//    }
}
