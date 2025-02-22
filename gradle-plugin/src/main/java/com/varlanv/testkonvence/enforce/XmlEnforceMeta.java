package com.varlanv.testkonvence.enforce;

import lombok.SneakyThrows;
import lombok.Value;
import lombok.val;
import lombok.var;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Value
public class XmlEnforceMeta {

    @SneakyThrows
    public List<APEnforcementMeta.Item> items(Path path) {
        return items(
            Files.newInputStream(path)
        );
    }

    @SneakyThrows
    public List<APEnforcementMeta.Item> items(InputStream inputStream) {
        val bas = new ByteArrayInputStream(readAllBytes(inputStream));
        val bytes = readAllBytes(bas);

        if (bytes.length == 0) {
            return Collections.emptyList();
        }
        val builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        val document = builder.parse(new ByteArrayInputStream(bytes));
        val root = document.getFirstChild();
        if (root == null) {
            return Collections.emptyList();
        }
        val entries = root.getChildNodes();
        val entriesLen = entries.getLength();
        val entriesList = new ArrayList<APEnforcementMeta.Item>(entriesLen);
        for (var entryIdx = 0; entryIdx < entriesLen; entryIdx++) {
            val entryNode = entries.item(entryIdx);
            val fields = entryNode.getChildNodes();
            entriesList.add(
                new APEnforcementMeta.Item(
                    fields.item(0).getTextContent(),
                    fields.item(1).getTextContent(),
                    fields.item(2).getTextContent(),
                    fields.item(3).getTextContent()
                )
            );
        }
        return entriesList;
    }

    @SneakyThrows
    private byte[] readAllBytes(InputStream is) {
        try (val bis = new BufferedInputStream(is)) {
            val out = new ByteArrayOutputStream();
            int i;
            while ((i = bis.read()) != -1) {
                out.write(i);
            }
            return out.toByteArray();
        }
    }
}
