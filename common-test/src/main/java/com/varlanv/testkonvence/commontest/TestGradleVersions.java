package com.varlanv.testkonvence.commontest;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TestGradleVersions {

    public static List<String> list() {
        return List.of(
            current(),
            latest7()
//            latest6()
        );
    }

    public static String current() {
        return latest8();
    }

    public static String latest7() {
        return "7.6.1";
    }

    public static String latest8() {
        return "8.12.1";
    }

    public static String latest6() {
        return "6.9.4";
    }
}
