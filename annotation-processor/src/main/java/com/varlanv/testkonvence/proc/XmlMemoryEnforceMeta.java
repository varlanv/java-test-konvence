package com.varlanv.testkonvence.proc;

import java.io.StringWriter;
import java.io.Writer;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

class XmlMemoryEnforceMeta {

    private final ImmutableList<APEnforcementMeta.Item> entries;

    XmlMemoryEnforceMeta(ImmutableList<APEnforcementMeta.Item> entries) {
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

        for (var entry : entries.value()) {
            var entryEl = document.createElement("entry");
            appendTo(entryEl, "fullEnclosingClassName", entry.fullEnclosingClassName(), document);
            appendTo(entryEl, "displayName", entry.displayName(), document);
            appendTo(entryEl, "className", entry.className(), document);
            appendTo(entryEl, "methodName", entry.methodName(), document);
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

    @Override
    public String toString() {
        try {
            var stringWriter = new StringWriter();
            write(stringWriter, true);
            return stringWriter.toString();
        } catch (Exception e) {
            return InternalUtils.hide(e);
        }
    }
}
