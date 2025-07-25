package com.varlanv.testkonvence;

public final class Constants {

    private Constants() {}

    public static final String performanceLogProperty = "com.varlanv.testkonvence.undocumented.performanceLog";
    public static final String maxMethodNameLengthProperty = "com.varlanv.testkonvence.maxMethodNameLength";
    public static final String apEnforcementsXmlPackage = "com.varlanv.testkonvence";
    public static final String apEnforcementsXmlName = "testkonvence_enforcements.xml";
    public static final String apIndentXmlOption = "com.varlanv.testkonvence.indentXml";
    public static final String apUseCamelCaseMethodNamesOption = "com.varlanv.testkonvence.camelCaseMethods";
    public static final String apReversedOption = "com.varlanv.testkonvence.reversed";
    public static final String PLUGIN_NAME = "com.varlanv.testkonvence";
    public static final String PLUGIN_VERSION = "1.0.9";
    public static final String PROCESSOR_JAR = "annotation-processor-" + PLUGIN_VERSION + ".jar";
    public static final String TEST_KONVENCE_TASK_GROUP = "test konvence";
    public static final String TEST_KONVENCE_VERIFY_TASK_NAME = "testKonvenceVerify";
    public static final String TEST_KONVENCE_APPLY_TASK_NAME = "testKonvenceApply";
    public static final String PROCESSOR_JAR_RESOURCE = "/" + PROCESSOR_JAR;
    public static final int DEFAULT_MAX_METHOD_NAME_LENGTH = 90;
}
