package com.varlanv.testkonvence.info;

import lombok.SneakyThrows;
import lombok.Value;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Value
public class XmlEnforceMeta {

    @SneakyThrows
    public List<EnforcementMeta.Item> items(Path path) {
        return items(
            Files.newInputStream(path)
        );
    }

    @SneakyThrows
    public List<EnforcementMeta.Item> items(InputStream inputStream) {
        var bas = new ByteArrayInputStream(inputStream.readAllBytes());
        var bytes = bas.readAllBytes();

        if (bytes.length == 0) {
            return List.of();
        }
        var builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        var document = builder.parse(new ByteArrayInputStream(bytes));
        var root = document.getFirstChild();
        if (root == null) {
            return List.of();
        }
        var entries = root.getChildNodes();
        var entriesLen = entries.getLength();
        var entriesList = new ArrayList<EnforcementMeta.Item>(entriesLen);
        for (var entryIdx = 0; entryIdx < entriesLen; entryIdx++) {
            var entryNode = entries.item(entryIdx);
            var fields = entryNode.getChildNodes();
            entriesList.add(
                new EnforcementMeta.Item(
                    fields.item(0).getTextContent(),
                    fields.item(1).getTextContent(),
                    fields.item(2).getTextContent(),
                    fields.item(3).getTextContent()
                )
            );
        }
        return entriesList;
    }
}
