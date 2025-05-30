package com.varlanv.testkonvence.commontest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.UnknownTaskException;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.services.BuildService;
import org.gradle.api.services.BuildServiceParameters;
import org.gradle.api.services.BuildServiceRegistration;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.testfixtures.ProjectBuilder;
import org.immutables.value.Value;
import org.jetbrains.annotations.Nullable;

public interface GradleIntegrationTest extends IntegrationTest {

    default void runProjectFixture(ThrowingConsumer<SingleProjectFixture> fixtureConsumer) {
        configureAndRunProjectFixture(ProjectBuilder::build, fixtureConsumer);
    }

    default void configureProjectDirAndRunProjectFixture(
            ThrowingConsumer<Path> projectDirConsumer, ThrowingConsumer<SingleProjectFixture> fixtureConsumer) {
        configureDirAndProjectAndRunProjectFixture(projectDirConsumer, ProjectBuilder::build, fixtureConsumer);
    }

    default void configureAndRunProjectFixture(
            Function<ProjectBuilder, Project> projectBuilderFn,
            ThrowingConsumer<SingleProjectFixture> fixtureConsumer) {
        configureDirAndProjectAndRunProjectFixture(pb -> {}, projectBuilderFn, fixtureConsumer);
    }

    default void configureDirAndProjectAndRunProjectFixture(
            ThrowingConsumer<Path> projectDirConsumer,
            Function<ProjectBuilder, Project> projectBuilderFn,
            ThrowingConsumer<SingleProjectFixture> fixtureConsumer) {
        var projectDir = newTempDir();
        runAndDeleteFile(projectDir, () -> {
            projectDirConsumer.accept(projectDir);
            var projectBuilder = ProjectBuilder.builder()
                    .withProjectDir(projectDir.toFile())
                    .withGradleUserHomeDir(projectDir.toFile());
            var project = projectBuilderFn.apply(projectBuilder);
            fixtureConsumer.accept(new SingleProjectFixture(projectDir, project));
        });
    }

    default void runProjectWithParentFixture(Consumer<ProjectWithParentFixture> fixtureConsumer) {
        BaseTest.runQuiet(() -> {
            var parentProjectDirectory = newTempDir();
            runAndDeleteFile(parentProjectDirectory, () -> {
                var projectDirectory = Files.createDirectory(parentProjectDirectory.resolve("gradle_test"));
                var parentProjectDirectoryFile = parentProjectDirectory.toFile();
                var parentProject = ProjectBuilder.builder()
                        .withProjectDir(parentProjectDirectoryFile)
                        .build();
                var project = ProjectBuilder.builder()
                        .withProjectDir(projectDirectory.toFile())
                        .withParent(parentProject)
                        .build();
                fixtureConsumer.accept(ImmutableProjectWithParentFixture.of(
                        project,
                        parentProject,
                        projectDirectory,
                        parentProjectDirectory,
                        project.getObjects(),
                        project.getProviders()));
            });
        });
    }

    default Project evaluateProject(Project project) {
        return ((ProjectInternal) project).evaluate();
    }

    record SingleProjectFixture(Path projectDir, Project project, ObjectFactory objects, ProviderFactory providers) {

        private SingleProjectFixture(Path projectDir, Project project) {
            this(projectDir, project, project.getObjects(), project.getProviders());
        }
    }

    @Value.Immutable(builder = false)
    interface ProjectWithParentFixture {

        @Value.Parameter
        Project project();

        @Value.Parameter
        Project parentProject();

        @Value.Parameter
        Path projectDir();

        @Value.Parameter
        Path parentProjectDir();

        @Value.Parameter
        ObjectFactory objects();

        @Value.Parameter
        ProviderFactory providers();
    }

    default <T extends Task> TaskProviderAssertions<T> gradleAssert(TaskProvider<T> taskProvider) {
        return new TaskProviderAssertions<>(taskProvider);
    }

    default <T extends Task> TaskAssertions<T> gradleAssert(T task) {
        return new TaskAssertions<>(task);
    }

    default ProjectAssertions gradleAssert(Project project) {
        return new ProjectAssertions(project);
    }

    class TaskProviderAssertions<SELF extends Task> {

        private final TaskProvider<SELF> subject;
        private @Nullable TaskAssertions<SELF> taskAssertions;

        public TaskProviderAssertions(TaskProvider<SELF> subject) {
            this.subject = subject;
        }

        public TaskProviderAssertions<SELF> dependsOn(TaskProvider<? extends Task> taskProvider) {
            getTaskAssertions().dependsOn(taskProvider);
            return this;
        }

        public TaskProviderAssertions<SELF> mustRunAfter(TaskProvider<? extends Task> taskProvider) {
            getTaskAssertions().mustRunAfter(taskProvider);
            return this;
        }

        public <T extends Provider<? extends BuildService<?>>> TaskProviderAssertions<SELF> requiresService(
                T buildServiceProvider) {
            getTaskAssertions().requiresService(buildServiceProvider);
            return this;
        }

        private TaskAssertions<SELF> getTaskAssertions() {
            if (taskAssertions == null) {
                taskAssertions = new TaskAssertions<>(subject.get());
            }
            return taskAssertions;
        }
    }

    class TaskAssertions<SELF extends Task> {

        private final SELF subject;

        public TaskAssertions(SELF subject) {
            this.subject = subject;
        }

        public TaskAssertions<SELF> dependsOn(TaskProvider<? extends Task> taskProvider) {
            var dependsOn = subject.getDependsOn();
            assertThat(dependsOn).contains(taskProvider);
            return this;
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        public TaskAssertions<SELF> mustRunAfter(TaskProvider<? extends Task> taskProvider) {
            var dependencies = (Set) subject.getMustRunAfter().getDependencies(null);
            Consumer<Set> containsTaskProvider = deps -> assertThat(deps).contains(taskProvider);
            Consumer<Set> containsTask = deps -> assertThat(deps).contains(taskProvider.get());
            Consumer<Set> containsTaskName = deps -> assertThat(deps).contains(taskProvider.getName());
            assertThat(dependencies).satisfiesAnyOf(containsTaskProvider, containsTaskName, containsTask);
            return this;
        }

        public <T extends Provider<? extends BuildService<?>>> TaskAssertions<SELF> requiresService(
                T buildServiceProvider) {
            var requiredServices = ((DefaultTask) subject).getRequiredServices();
            var serviceRequired = requiredServices.isServiceRequired(buildServiceProvider);
            assertThat(serviceRequired).isTrue();
            return this;
        }
    }

    class ProjectAssertions {

        private final Project project;

        public ProjectAssertions(Project project) {
            this.project = project;
        }

        public ProjectAssertions doesNotHaveTask(String taskName) {
            assertThatThrownBy(() -> project.getTasks().named(taskName))
                    .isInstanceOf(UnknownTaskException.class)
                    .hasMessageContaining(taskName);
            return this;
        }

        public ProjectAssertions hasOnlyOneService(String name) {
            var buildServices =
                    new ArrayList<>(project.getGradle().getSharedServices().getRegistrations());
            assertThat(buildServices).hasSize(1);
            assertThat(buildServices.get(0).getName()).isEqualTo(name);
            return this;
        }

        @SuppressWarnings("unchecked")
        public <
                        BSP extends BuildServiceParameters,
                        BS extends BuildService<BSP>,
                        BSR extends BuildServiceRegistration<BS, BSP>>
                ProjectAssertions withBuildServiceRegistration(String name, ThrowingConsumer<BSR> consumer) {
            var registrations =
                    List.copyOf(project.getGradle().getSharedServices().getRegistrations());
            var maybeBuildServiceRegistration = registrations.stream()
                    .filter(reg -> reg.getName().equals(name))
                    .findFirst();
            assertThat(maybeBuildServiceRegistration).isPresent();
            var buildServiceRegistration = maybeBuildServiceRegistration.get();
            consumer.toUnchecked().accept((BSR) buildServiceRegistration);
            return this;
        }

        public ProjectAssertions hasExtensionWithName(String name) {
            assertThat(project.getExtensions().findByName(name)).isNotNull();
            return this;
        }

        public ProjectAssertions hasExtensionWithType(Class<?> type) {
            assertThat(project.getExtensions().findByType(type)).isNotNull();
            return this;
        }
    }
}
