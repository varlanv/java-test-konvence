package com.varlanv.testnameconvention.gradle.plugin;

import com.varlanv.testnameconvention.commontest.DataTable;
import com.varlanv.testnameconvention.commontest.FunctionalTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.file.Files;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestNameConventionPluginFunctionalTest implements FunctionalTest {

    @ParameterizedTest
    @MethodSource("defaultDataTables")
    void abc(DataTable dataTable) {
        runGradleRunnerFixture(
            dataTable,
            List.of("help"),
            (fixture) -> {
                Files.writeString(fixture.rootBuildFile(), """
                    plugins {
                        id("com.varlanv.testnameconvention-gradle-plugin")
                    }
                    """);
                var result = build(fixture.runner());

                assertThat(result.getOutput()).contains("Hello World!");
            }
        );
    }
}
