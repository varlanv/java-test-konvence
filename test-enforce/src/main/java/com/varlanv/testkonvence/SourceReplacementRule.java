package com.varlanv.testkonvence;

import java.util.List;

public interface SourceReplacementRule {

    SourceFile target();

    List<String> apply(List<String> list);

    Boolean isNoop();
}
