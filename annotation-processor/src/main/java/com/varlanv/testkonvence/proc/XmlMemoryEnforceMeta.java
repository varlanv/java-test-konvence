package com.varlanv.testkonvence.proc;

import com.varlanv.testkonvence.APEnforcementFull;
import com.varlanv.testkonvence.APEnforcementMiddle;
import com.varlanv.testkonvence.APEnforcementTop;
import com.varlanv.testkonvence.ImmutableList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class XmlMemoryEnforceMeta {

    private static final String fullEnclosingClassNameProp = "a";
    private static final String classNameProp = "b";
    private static final String displayNameProp = "c";
    private static final String methodNameProp = "d";
    private static final String newNameProp = "e";
    private static final String reversedProp = "f";
    private static final String methodEnforcementsProp = "g";
    private static final String entriesProp = "h";
    private final ImmutableList<APEnforcementFull> entries;

    XmlMemoryEnforceMeta(ImmutableList<APEnforcementFull> entries) {
        this.entries = entries;
    }

    public void writeTo(Writer writer) throws Exception {
        write(writer, false);
    }

    public void indentWriteTo(Writer writer) throws Exception {
        write(writer, true);
    }

    private void write(Writer writer, boolean indent) throws Exception {
        if (entries.value().isEmpty()) {
            writer.write("");
            writer.flush();
            return;
        }
        var documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        var document = documentBuilder.newDocument();
        var root = document.createElement("root");
        document.appendChild(root);
        var documentEnforcement = document.createElement(methodEnforcementsProp);
        root.appendChild(documentEnforcement);


        for (var entry : entries.value()) {
            var entryEl = document.createElement("entry");
            appendTo(entryEl, "fullEnclosingClassName", entry.fullEnclosingClassName(), document);
            appendTo(entryEl, "displayName", entry.displayName(), document);
            appendTo(entryEl, "className", entry.className(), document);
            appendTo(entryEl, "methodName", entry.originalName(), document);
            root.appendChild(entryEl);
        }

        var tf = TransformerFactory.newInstance();
        var transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.INDENT, indent ? "yes" : "no");
        transformer.transform(new DOMSource(document), new StreamResult(writer));
        writer.flush();
    }

    private void appendTo(Element target, String name, String value, Document document) {
        var displayName = document.createElement(name);
        displayName.setTextContent(value);
        target.appendChild(displayName);
    }

    private ImmutableList<APEnforcementTop> merge() {
        Map<String, List<APEnforcementMiddle>> middleMap = new HashMap<>();
        for (var entry : entries) {
            middleMap.computeIfAbsent(entry.fullEnclosingClassName(), k -> new ArrayList<>())
                .add(APEnforcementMiddle);
        }
    }
}
