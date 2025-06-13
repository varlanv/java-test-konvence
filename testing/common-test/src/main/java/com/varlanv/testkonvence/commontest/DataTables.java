package com.varlanv.testkonvence.commontest;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.immutables.value.Value;

@Value.Immutable(builder = false)
public interface DataTables {

    @Value.Parameter
    ImmutableList<Boolean> isCiList();

    @Value.Parameter
    ImmutableList<Boolean> configurationCacheList();

    @Value.Parameter
    ImmutableList<Boolean> buildCacheList();

    @Value.Parameter
    ImmutableList<String> gradleVersions();

    static Stream<DataTable> streamDefault() {
        return getDefault().list().stream();
    }

    static DataTables getDefault() {
        //        if (Objects.equals(System.getenv("CI"), "true")) {
        //        if (true) {
        return ImmutableDataTables.of(
                ImmutableList.of(false),
                ImmutableList.of(false),
                ImmutableList.of(false),
                ImmutableList.of(TestGradleVersions.current()));
        //        } else {
        //            return new DataTables(
        //                List.of(true, false),
        //                List.of(true, false),
        //                List.of(true, false),
        //                TestGradleVersions.list()
        //            );
    }

    default List<DataTable> list() {
        List<DataTable> result = new ArrayList<>();
        gradleVersions().forEach(gradleVersion -> isCiList()
                .forEach(isCi -> configurationCacheList().forEach(configurationCache -> buildCacheList()
                        .forEach(buildCache ->
                                result.add(new DataTable(isCi, configurationCache, buildCache, gradleVersion))))));
        return result;
    }
}
