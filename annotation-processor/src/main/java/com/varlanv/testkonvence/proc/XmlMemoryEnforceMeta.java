package com.varlanv.testkonvence.proc;

import com.varlanv.testkonvence.APEnforcementTop;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.Writer;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

final class XmlMemoryEnforceMeta {

    private static final String methodEnforcementsElement = "q";
    private static final String topEntriesElement = "w";
    private static final String fullEnclosingClassNameElement = "e";
    private static final String classEntriesElement = "r";
    private static final String classEntryElement = "r";
    private static final String methodEntriesElement = "t";
    private static final String methodEntryElement = "y";
    private static final String classNameProp = "u";
    private static final String fullEnclosingClassNameProp = "i";
    private static final String displayNameProp = "o";
    private static final String methodNameProp = "p";
    private static final String newNameProp = "a";
    private final Collection<APEnforcementTop> entries;

    XmlMemoryEnforceMeta(Collection<APEnforcementTop> entries) {
        this.entries = entries;
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
                topEl.appendChild(classEl);
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
}
