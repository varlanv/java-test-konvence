package com.varlanv.testkonvence.gradle.plugin;

import com.varlanv.testkonvence.ImmutableList;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.xml.parsers.DocumentBuilderFactory;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

final class XmlEnforceMeta {

    public List<APEnforcementMeta.Item> items(Path path) throws Exception {
        try (var inputStream = Files.newInputStream(path)) {
            return items(inputStream);
        }
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
        var childNodes = root.getChildNodes();
        var nodes = nodeList(childNodes).value();
        var entriesList = new ArrayList<APEnforcementMeta.Item>(childNodes.getLength());
        for (var node : nodes) {
            List<@NonNull String> fields = textContents(node.getChildNodes()).value();
            entriesList.add(ImmutableItem.of(fields.get(0), fields.get(1), fields.get(2), fields.get(3)));
        }
        return entriesList;
    }

    private ImmutableList<String> textContents(@Nullable NodeList nodeList) {
        var children = nodeList(nodeList);
        return children.mapOptional((Node node) -> Optional.<String>ofNullable(node.getTextContent()));
    }

    private ImmutableList<Node> nodeList(@Nullable NodeList nodeList) {
        if (nodeList == null) {
            return ImmutableList.empty();
        }
        var entriesLen = nodeList.getLength();
        var result = new ArrayList<@Nullable Node>(entriesLen);
        for (var entryIdx = 0; entryIdx < entriesLen; entryIdx++) {
            var entryNode = nodeList.item(entryIdx);
            result.add(entryNode);
        }
        return ImmutableList.copyOfIgnoringNulls(result);
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
