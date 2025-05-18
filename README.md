# Gradle plugin for enforcing test naming convention.

A gradle plugin that provides a way to automatically change test method
names based on `@DisplayName` annotation, as well as doing opposite - automatically generate
`@DisplayName` based on test method name.

## Usage

For the most basic use-case, apply the plugin:

In `build.gradle(.kts)`

```kotlin
plugins {
    id("com.varlanv.test-konvence") version "0.0.1"
}
```

The plugin will add two tasks:

1. **testKonvenceEnforceAll** - task for changing test method names to match JUnit `@DisplayName` annotation,
   and generate `@DisplayName` annotation based on method name where it doesn't yet exist.
2. **testKonvenceDryEnforceWithFailing** - task for failing build in case there are tests, for which
   test name transformation is not yet applied, either method name to `@DisplayName`, or `@DisplayName` to method name.

The plugin will also configure automatic applying of `testKonvenceEnforceAll` task after each test run.

Consider following test class:

```java
class StringTest {

    @Test
    void should_equal_hello_has_length_5() {
        Assertions.assertEquals(5, "hello".length());
    }
}
```

After running `test` task, or running `testKonvenceEnforceAll` task, two things will be added:

1. If not already present, `import org.junit.jupiter.api.DisplayName` dependency will be added
2. `@DisplayName` will be generated from test name.

```java
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class StringTest {

    @Test
    @DisplayName("should contain 2 chars in word hello")
    void should_contain_2_chars_in_word_hello() {
        Assertions.assertEquals(5, "hello".length());
    }
}
```

Now, consider this test class:

```java
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class StringTest {

    @Test
    @DisplayName("should contain 2 chars in word 'hello'")
    void should_work() {
        Assertions.assertEquals(5, "hello".length());
    }
}
```

After running `test` task, or running `testKonvenceEnforceAll` task,
since there is mismatch between what method name and what is written in `@DisplayName`,
the class will be rewritten to following:

```java
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class StringTest {

    @Test
    @DisplayName("should contain 2 chars in word 'hello'")
    void should_contain_2_chars_in_word_hello() {
        Assertions.assertEquals(5, "hello".length());
    }
}
```

## Configuration

Here is an exhaustive list of available configurations:

In `build.gradle(.kts)`

```kotlin
testKonvence {
    enabled(false)
    applyAutomaticallyAfterTestTask(true)
    useCamelCaseForMethodNames(false)
    reverseTransformation {
        enabled(true)
    }
}
```

* **enabled** - whether to apply plugin logic. If set to **false**,
  tasks `testKonvenceEnforceAll` and `testKonvenceDryEnforceWithFailing`
  will still be created, but will have no-op action. Default is **true**.
* **applyAutomaticallyAfterTestTask** - if set to **true**, all tests will be
  automatically renamed after `test` task. If set to **false**,
  applying test naming tranformations will be available only through `testKonvenceEnforceAll` task
  Default is **true**.
* **useCamelCaseForMethodNames** - if is to **true**, camel case method names will be used,
  instead of the default snake case. Default is **false**.
* **reverseTransformation.enabled** - if set to **true**, `@DisplayName` annotation will be
generated based on test method name. If set to **false**, only method name transformations
based on existing `@DisplayName` annotations will be applied. Default is **true**.

## Gradle optimizations support

The plugin is built to support all the major Gradle optimization features, such as:

* [Parallel builds](https://docs.gradle.org/current/userguide/performance.html#parallel_execution)
* [Build cache](https://docs.gradle.org/current/userguide/build_cache.html)
* [Configuration cache](https://docs.gradle.org/current/userguide/configuration_cache.html)
* [Isolated projects](https://docs.gradle.org/current/userguide/isolated_projects.html)

## Known limitations

- Currently, only JUnit 5 is supported
- Synchronization is possible only by using Junit tags (`org.junit.jupiter.api.Tag`).
- The plugin was tested with latest Gradle 8x, 7x, 6x versions (8.12.1, 7.6.1, 6.9.4). Any other version is not
  guaranteed to work. But most likely any version in range 7.x.x - 8.x.x will work, since plugin does not rely on any
  unstable internal Gradle API.

If you have any issues or feature requests, please don't hesitate to create an issue.

## License

This project is distributed under the [MIT License](LICENSE).
