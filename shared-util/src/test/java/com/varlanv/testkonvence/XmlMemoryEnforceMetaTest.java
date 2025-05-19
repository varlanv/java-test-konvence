package com.varlanv.testkonvence;

import static org.assertj.core.api.Assertions.assertThat;

import com.varlanv.testkonvence.commontest.BaseTest;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.TreeSet;
import org.junit.jupiter.api.Test;

class XmlMemoryEnforceMetaTest implements BaseTest {

    private static final XmlMemoryEnforceMeta defaultSubject =
            XmlMemoryEnforceMeta.fromEntriesCollection(List.of(creatEnforcementTop(0), creatEnforcementTop(1)));

    private static APEnforcementTop creatEnforcementTop(int index) {
        return ImmutableAPEnforcementTop.of(
                "topClassName" + index,
                new TreeSet<>(List.of(ImmutableAPEnforcementMiddle.of(
                        "someClassName" + index,
                        new TreeSet<>(List.of(ImmutableAPEnforcementItem.builder()
                                .displayName("dName" + index)
                                .originalName("oName" + index)
                                .newName("nName" + index)
                                .build()))))));
    }

    @Test
    void should_create_correct_xml_string() throws Exception {
        var writer = new StringWriter();

        defaultSubject.writeTo(writer);

        assertThat(writer.toString())
                .isEqualToIgnoringWhitespace(
                        """
            <root>
                <q>
                    <w>
                        <e>
                            <o>topClassName0</o>
                            <r>
                                <t>
                                    <i>someClassName0</i>
                                    <y>
                                        <u>
                                            <p>dName0</p>
                                            <a>oName0</a>
                                            <s>nName0</s>
                                        </u>
                                    </y>
                                </t>
                            </r>
                        </e>
                        <e>
                            <o>topClassName1</o>
                            <r>
                                <t>
                                    <i>someClassName1</i>
                                    <y>
                                        <u>
                                            <p>dName1</p>
                                            <a>oName1</a>
                                            <s>nName1</s>
                                        </u>
                                    </y>
                                </t>
                            </r>
                        </e>
                    </w>
                </q>
            </root>""");
    }

    @Test
    void should_equal_to_self_when_write_to_and_then_read_from_xml() throws Exception {
        var writer = new StringWriter();

        defaultSubject.writeTo(writer);

        var actual = XmlMemoryEnforceMeta.fromXmlStream(
                new ByteArrayInputStream(writer.toString().getBytes(StandardCharsets.UTF_8)));

        assertThat(actual.entries()).isEqualTo(defaultSubject.entries());
    }
}
