package com.varlanv.testkonvence;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

interface InternalUtils {

    static byte[] readAllBytes(InputStream is) throws Exception {
        try (var bis = new BufferedInputStream(is)) {
            var out = new ByteArrayOutputStream();
            int i;
            while ((i = bis.read()) != -1) {
                out.write(i);
            }
            return out.toByteArray();
        }
    }
}
