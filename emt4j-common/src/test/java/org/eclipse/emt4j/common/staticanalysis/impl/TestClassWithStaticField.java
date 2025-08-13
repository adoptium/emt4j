package org.eclipse.emt4j.common.staticanalysis.impl;

public class TestClassWithStaticField {
    static int myField;
    static {
        // Doesn’t matter what the RHS is anymore; we’re asserting body-release behavior.
        int tmp = 42;
        myField = tmp;
    }
}
