//package com.varlanv.testnameconvention;
//
//import com.varlanv.testnameconvention.commontest.UnitTest;
//import com.varlanv.testnameconvention.info.EnforcementMeta;
//import com.varlanv.testnameconvention.info.XmlMemoryEnforceMeta;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Test;
//
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//class EnforcedFileTest implements UnitTest {
//
//    @Nested
//    class SingleTestReplacement {
//
//        String sources = """
//            package com.varlanv.testnameconvention;
//
//            import org.assertj.core.api.Assertions;
//            import org.junit.jupiter.api.DisplayName;
//            import org.junit.jupiter.api.Test;
//
//            public class SomeTest {
//
//                @Test
//                @DisplayName("Very descriptive test name!")
//                void something() {
//                    Assertions.assertThat(true).isTrue();
//                }
//            }""";
//
//        @Test
//        void should_return_same_sources_for_empty_enforcements_list() {
//            var enforcedFile = new EnforcedFile(
//                sources,
//                new XmlMemoryEnforceMeta(
//                    List.of()
//                )
//            );
//            assertThat(enforcedFile.enforced()).isEqualTo(sources);
//        }
//
//        @Test
//        void should_replace_test_name_with_new_value() {
//            var enforcedFile = new EnforcedFile(
//                sources,
//                new XmlMemoryEnforceMeta(
//                    List.of(
//                        new EnforcementMeta.Enforcement(
//                            "something",
//                            "Very descriptive test name!",
//                            "SomeTest",
//                            "very_descriptive_test_name"
//                        )
//                    )
//                )
//            );
//
//            assertThat(enforcedFile.enforced()).isEqualTo(
//                """
//                    package com.varlanv.testnameconvention;
//
//                    import org.assertj.core.api.Assertions;
//                    import org.junit.jupiter.api.DisplayName;
//                    import org.junit.jupiter.api.Test;
//
//                    public class SomeTest {
//
//                        @Test
//                        @DisplayName("Very descriptive test name!")
//                        void very_descriptive_test_name() {
//                            Assertions.assertThat(true).isTrue();
//                        }
//                    }"""
//            );
//        }
//    }
//}
