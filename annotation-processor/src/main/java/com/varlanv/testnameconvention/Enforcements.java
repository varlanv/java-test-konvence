package com.varlanv.testnameconvention;

import lombok.Value;

@Value
public class Enforcements {

    Boolean capitalizeTestName;
    Boolean whenThenPattern;
    Boolean failOnMismatch;
    EnforcedPatterns enforcedPatterns;
}
