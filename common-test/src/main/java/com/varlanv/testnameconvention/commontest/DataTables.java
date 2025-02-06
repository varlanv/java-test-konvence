package com.varlanv.testnameconvention.commontest;

import lombok.Builder;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Builder(toBuilder = true)
@RequiredArgsConstructor
public final class DataTables {

    List<Boolean> isCiList;
    List<Boolean> configurationCacheList;
    List<Boolean> buildCacheList;
    List<String> gradleVersions;

    public DataTables(List<Boolean> isCiList, List<Boolean> configurationCacheList, List<Boolean> buildCacheList) {
        this(isCiList, configurationCacheList, buildCacheList, TestGradleVersions.list());
    }

    public static Stream<DataTable> streamDefault() {
        return getDefault().list().stream();
    }

    public static DataTables getDefault() {
        if (Objects.equals(System.getenv("CI"), "true")) {
            return new DataTables(
                List.of(true),
                List.of(true),
                List.of(true),
                List.of(TestGradleVersions.current()
                ));
        } else {
            return new DataTables(
                List.of(true),
                List.of(true),
                List.of(true),
                TestGradleVersions.list());
        }
    }

    public DataTables isCiAlwaysFalse() {
        return toBuilder().isCiList(List.of(false)).build();
    }

    public DataTables configurationCacheAlwaysFalse() {
        return toBuilder().configurationCacheList(List.of(false)).build();
    }

    public DataTables configurationCacheAlwaysTrue() {
        return toBuilder().configurationCacheList(List.of(true)).build();
    }

    public DataTables isCiAlwaysTrue() {
        return toBuilder().isCiList(List.of(true)).build();
    }

    public DataTables buildCacheAlwaysTrue() {
        return toBuilder().buildCacheList(List.of(true)).build();
    }

    public DataTables buildCacheAlwaysFalse() {
        return toBuilder().buildCacheList(List.of(false)).build();
    }

    public List<DataTable> list() {
        List<DataTable> result = new ArrayList<>();
        gradleVersions.forEach(gradleVersion ->
            isCiList.forEach(isCi ->
                configurationCacheList.forEach(configurationCache ->
                    buildCacheList.forEach(buildCache ->
                        result.add(new DataTable(isCi, configurationCache, buildCache, gradleVersion))
                    )
                )
            )
        );
        return result;
    }
}
