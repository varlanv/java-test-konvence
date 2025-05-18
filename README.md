# Gradle plugin for enforcing test naming convention.

A gradle plugin that provides a way to automatically change test method
names based on `@DisplayName` annotation, as well as doing opposite - automatically generate
`@DisplayName` based on test method name.

## Usage

For the most basic use-case, apply the plugin:

In `build.gradle(.kts)`:

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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class StringTest {

    @Test
    void should_contain_5_chars_in_word_hello() {
        Assertions.assertEquals(5, "hello".length());
    }
}
```

After running `test` task, or running `testKonvenceEnforceAll` task, two things will be added:

1. If not already present, `import org.junit.jupiter.api.DisplayName` import will be added
2. `@DisplayName` will be generated from test name.

```java
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class StringTest {

    @Test
    @DisplayName("should contain 5 chars in word hello")
    void should_contain_5_chars_in_word_hello() {
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
    @DisplayName("should contain 5 chars in word 'hello'")
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
    @DisplayName("should contain 5 chars in word 'hello'")
    void should_contain_5_chars_in_word_hello() {
        Assertions.assertEquals(5, "hello".length());
    }
}
```

## Usage example for CI/CD pipelines

In `build.gradle(.kts)`:

```kotlin
plugins {
    id("com.varlanv.test-konvence") version ("0.0.1")
}

testKonvence {
    applyAutomaticallyAfterTestTask(!providers.environmentVariable("CI").isPresent())
}
```

Then, somewhere in CI / CD configuration, add a step:
`./gradlew testKonvenceDryEnforceWithFailing`.

Configuring a plugin like this will make it so that developers will
have nice developer experience on local machines, with test naming
enforced on each test run. Meanwhile, the CI / CD pipeline will enforce that
test naming is always consistent and will fail the build in case a developer forgot to
apply test naming transformations.

## Configurations

Here is an exhaustive list of available configurations:

In `build.gradle(.kts)`:

```kotlin
testKonvence {
    enabled(true)
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
  applying test naming tranformations will be available only through `testKonvenceEnforceAll` task.
  Default is **true**.
* **useCamelCaseForMethodNames** - if is to **true**, camel case method names will be used,
  instead of the default snake case. Default is **false**.
* **reverseTransformation.enabled** - if set to **true**, `@DisplayName` annotation will be
  generated based on test method name. If set to **false**, only method name transformations
  based on existing `@DisplayName` annotations will be applied. Default is **true**.

## Transformations examples

#### `@DisplayName` to snake case method name

* **a_b_c** → _a_b_c_
* **Some good test name** → _some_good_test_name_
* **Some good test name, which has comas, dots., question? marks, and exclamation
  marks!** → _some_good_test_name_which_has_comas_dots_question_marks_and_exclamation_marks_
* **Some123good456test789name0** → _some123good456test789name0_

#### Snake case method name to `@DisplayName`

* _when_something_then_should_something_ → **when something then should something**
* _when_call_someMethod_then_should_something_ → **when call 'someMethod' then should something**
* _someMethod_should_do_something_ → **'someMethod' should do something**
* ____someMethod___shouldDo___someThing___ → **'someMethod' 'shouldDo' 'someThing'**

## Gradle optimizations support

The plugin is built to support all the major Gradle optimization features, such as:

* [Parallel builds](https://docs.gradle.org/current/userguide/performance.html#parallel_execution)
* [Build cache](https://docs.gradle.org/current/userguide/build_cache.html)
* [Configuration cache](https://docs.gradle.org/current/userguide/configuration_cache.html)
* [Isolated projects](https://docs.gradle.org/current/userguide/isolated_projects.html)

## Known limitations

- Currently, only JUnit 5 is supported
- The plugin was tested with the latest Gradle 8x and 7x versions (8.14, 7.6.1). Any other version is not
  guaranteed to work, but in general, any version in range 7.6.1 - 8.x.x should work.

If you have any issues or feature requests, please don't hesitate to create an issue.

## License

This project is distributed under the [MIT License](LICENSE).
