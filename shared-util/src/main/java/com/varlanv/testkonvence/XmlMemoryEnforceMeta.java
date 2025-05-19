package com.varlanv.testkonvence;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.TreeSet;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class XmlMemoryEnforceMeta {

    private static final String methodEnforcementsElement = "q";
    private static final String topEntriesElement = "w";
    private static final String fullEnclosingClassNameElement = "e";
    private static final String classEntriesElement = "r";
    private static final String classEntryElement = "t";
    private static final String methodEntriesElement = "y";
    private static final String methodEntryElement = "u";
    private static final String classNameProp = "i";
    private static final String fullEnclosingClassNameProp = "o";
    private static final String displayNameProp = "p";
    private static final String methodNameProp = "a";
    private static final String newNameProp = "s";
    private final Collection<APEnforcementTop> entries;

    private XmlMemoryEnforceMeta(Collection<APEnforcementTop> entries) {
        this.entries = entries;
    }

    public static XmlMemoryEnforceMeta fromEntriesCollection(Collection<APEnforcementTop> entries) {
        return new XmlMemoryEnforceMeta(entries);
    }

    public static XmlMemoryEnforceMeta fromXmlPath(Path path) throws Exception {
        try (var inputStream = Files.newInputStream(path)) {
            return fromXmlStream(inputStream);
        }
    }

    public static XmlMemoryEnforceMeta fromXmlStream(InputStream inputStream) throws Exception {
        var bytes = InternalUtils.readAllBytes(inputStream);

        if (bytes.length == 0) {
            return new XmlMemoryEnforceMeta(Collections.emptyList());
        }
        var builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        var document = builder.parse(new ByteArrayInputStream(bytes));
        var parsed = parse(document);
        if (parsed == null) {
            throw new ParseException("Failed to parse given xml", -1);
        }
        return parsed;
    }

    public Collection<APEnforcementTop> entries() {
        return entries;
    }

    public void writeTo(Writer writer) throws Exception {
        write(writer, false);
    }

    public void indentWriteTo(Writer writer) throws Exception {
        write(writer, true);
    }

    private void write(Writer writer, boolean indent) throws Exception {
        if (entries.isEmpty()) {
            writer.write("");
            writer.flush();
            return;
        }
        var documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        var document = documentBuilder.newDocument();
        var rootEl = document.createElement("root");
        document.appendChild(rootEl);
        var documentEnforcementEl = document.createElement(methodEnforcementsElement);
        rootEl.appendChild(documentEnforcementEl);
        var topEntriesEl = document.createElement(topEntriesElement);
        documentEnforcementEl.appendChild(topEntriesEl);

        for (var topItem : entries) {
            var topEl = document.createElement(fullEnclosingClassNameElement);
            topEntriesEl.appendChild(topEl);
            appendStringTo(topEl, fullEnclosingClassNameProp, topItem.fullEnclosingClassName(), document);
            var classEntriesEl = document.createElement(classEntriesElement);
            topEl.appendChild(classEntriesEl);
            for (var classItem : topItem.classEnforcements()) {
                var classEl = document.createElement(classEntryElement);
                classEntriesEl.appendChild(classEl);
                appendStringTo(classEl, classNameProp, classItem.className(), document);
                var methodEntriesEl = document.createElement(methodEntriesElement);
                classEl.appendChild(methodEntriesEl);
                for (var methodItem : classItem.methodEnforcements()) {
                    var methodEl = document.createElement(methodEntryElement);
                    methodEntriesEl.appendChild(methodEl);
                    appendStringTo(methodEl, displayNameProp, methodItem.displayName(), document);
                    appendStringTo(methodEl, methodNameProp, methodItem.originalName(), document);
                    appendStringTo(methodEl, newNameProp, methodItem.newName(), document);
                }
            }
        }

        var tf = TransformerFactory.newInstance();
        var transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.INDENT, indent ? "yes" : "no");
        transformer.transform(new DOMSource(document), new StreamResult(writer));
        writer.flush();
    }

    private void appendStringTo(Element target, String name, String value, Document document) {
        var displayName = document.createElement(name);
        displayName.setTextContent(value);
        target.appendChild(displayName);
    }

    @Nullable private static XmlMemoryEnforceMeta parse(Document document) throws Exception {
        var rootNode = document.getFirstChild();
        if (rootNode == null) {
            return null;
        }
        var methodEnforcementsNode = rootNode.getFirstChild();
        if (methodEnforcementsNode == null) {
            return null;
        }
        var topEntriesNode = methodEnforcementsNode.getFirstChild();
        if (topEntriesNode == null) {
            return null;
        }
        var topEntries = new ArrayList<APEnforcementTop>();
        forEachNamed(topEntriesNode.getChildNodes(), (topNode, topName) -> {
            if (fullEnclosingClassNameElement.equals(topName)) {
                var classEnforcements = new TreeSet<APEnforcementMiddle>();
                var topBuilder = ImmutableAPEnforcementTop.builder().classEnforcements(classEnforcements);
                forEachNamed(topNode.getChildNodes(), (topClassNode, topClassNodeName) -> {
                    if (fullEnclosingClassNameProp.equals(topClassNodeName)) {
                        topBuilder.fullEnclosingClassName(topClassNode.getTextContent());
                    } else if (classEntriesElement.equals(topClassNodeName)) {
                        forEachNamed(topClassNode.getChildNodes(), (classEntriesNode, classEntriesNodeName) -> {
                            if (classEntryElement.equals(classEntriesNodeName)) {
                                var methodEnforcements = new TreeSet<APEnforcementItem>();
                                var middleBuilder =
                                        ImmutableAPEnforcementMiddle.builder().methodEnforcements(methodEnforcements);
                                forEachNamed(classEntriesNode.getChildNodes(), (classEntryNode, classEntryNodeName) -> {
                                    if (classNameProp.equals(classEntryNodeName)) {
                                        middleBuilder.className(classEntryNode.getTextContent());
                                    } else if (methodEntriesElement.equals(classEntryNodeName)) {
                                        forEachNamed(
                                                classEntryNode.getChildNodes(),
                                                (methodEntriesNode, methodEntriesNodeName) -> {
                                                    if (methodEntryElement.equals(methodEntriesNodeName)) {
                                                        var itemBuilder = ImmutableAPEnforcementItem.builder();
                                                        forEachNamed(
                                                                methodEntriesNode.getChildNodes(),
                                                                (methodEntryNode, methodEntryNodeName) -> {
                                                                    if (displayNameProp.equals(methodEntryNodeName)) {
                                                                        itemBuilder.displayName(
                                                                                methodEntryNode.getTextContent());
                                                                    } else if (methodNameProp.equals(
                                                                            methodEntryNodeName)) {
                                                                        itemBuilder.originalName(
                                                                                methodEntryNode.getTextContent());
                                                                    } else if (newNameProp.equals(
                                                                            methodEntryNodeName)) {
                                                                        itemBuilder.newName(
                                                                                methodEntryNode.getTextContent());
                                                                    }
                                                                });
                                                        methodEnforcements.add(itemBuilder.build());
                                                    }
                                                });
                                    }
                                });
                                classEnforcements.add(middleBuilder.build());
                            }
                        });
                    }
                });
                topEntries.add(topBuilder.build());
            }
        });
        return new XmlMemoryEnforceMeta(Collections.unmodifiableList(topEntries));
    }

    private static void forEachNamed(NodeList nodeList, ThrowingBiConsumer<@NonNull Node, @NonNull String> consumer)
            throws Exception {
        var len = nodeList.getLength();
        for (var idx = 0; idx < len; idx++) {
            var item = nodeList.item(idx);
            consumer.accept(item, item.getNodeName());
        }
    }
}
