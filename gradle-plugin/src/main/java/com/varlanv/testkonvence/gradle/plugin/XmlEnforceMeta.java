package com.varlanv.testkonvence.gradle.plugin;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;

final class XmlEnforceMeta {

    public List<APEnforcementMeta.Item> items(Path path) throws Exception {
        return items(Files.newInputStream(path));
    }

    public List<APEnforcementMeta.Item> items(InputStream inputStream) throws Exception {
        var bas = new ByteArrayInputStream(readAllBytes(inputStream));
        var bytes = readAllBytes(bas);

        if (bytes.length == 0) {
            return Collections.emptyList();
        }
        var builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        var document = builder.parse(new ByteArrayInputStream(bytes));
        var root = document.getFirstChild();
        if (root == null) {
            return Collections.emptyList();
        }
        var entries = root.getChildNodes();
        var entriesLen = entries.getLength();
        var entriesList = new ArrayList<APEnforcementMeta.Item>(entriesLen);
        for (var entryIdx = 0; entryIdx < entriesLen; entryIdx++) {
            var entryNode = entries.item(entryIdx);
            var fields = entryNode.getChildNodes();
            entriesList.add(ImmutableItem.of(
                    fields.item(0).getTextContent(),
                    fields.item(1).getTextContent(),
                    fields.item(2).getTextContent(),
                    fields.item(3).getTextContent()));
        }
        return entriesList;
    }

    private byte[] readAllBytes(InputStream is) throws Exception {
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
