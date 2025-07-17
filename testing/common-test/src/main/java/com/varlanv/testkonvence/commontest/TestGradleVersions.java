package com.varlanv.testkonvence.commontest;

import java.util.List;

public final class TestGradleVersions {

    private TestGradleVersions() {}

    public static List<String> list() {
        return List.of(current());
    }

    public static String current() {
        return latest8();
    }

    public static String latest7() {
        return "7.6.5";
    }

    public static String latest8() {
        return "8.14.3";
    }

    public static String latest6() {
        return "6.9.4";
    }
}
