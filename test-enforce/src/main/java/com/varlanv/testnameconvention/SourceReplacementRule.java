package com.varlanv.testnameconvention;

import java.util.List;

public interface SourceReplacementRule {

    SourceFile target();

    List<String> apply(List<String> list);

    Boolean isNoop();
}
