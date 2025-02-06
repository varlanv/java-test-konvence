//package com.varlanv.testnameconvention;
//
//import com.varlanv.testnameconvention.commontest.UnitTest;
//import com.varlanv.testnameconvention.info.EnforcementMeta;
//import com.varlanv.testnameconvention.info.XmlFileEnforceMeta;
//import com.varlanv.testnameconvention.info.XmlMemoryEnforceMeta;
//import org.junit.jupiter.api.Test;
//
//import java.nio.file.Files;
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//class XmlFileEnforcementMetaTest implements UnitTest {
//
//    @Test
//    void should_parse_xml_file() {
//        useTempFile(path -> {
//            var inMemory = new XmlMemoryEnforceMeta(
//                List.of(
//                    new EnforcementMeta.Enforcement(
//                        "someMethodName",
//                        "someDisplayName",
//                        "someClassName",
//                        "someMethodNameReplacement"
//                    )
//                )
//            );
//            inMemory.writeTo(Files.newBufferedWriter(path));
//            var xmlFile = new XmlFileEnforceMeta(path);
//            assertThat(xmlFile.rules()).isEqualTo(inMemory.rules());
//        });
//    }
//}
