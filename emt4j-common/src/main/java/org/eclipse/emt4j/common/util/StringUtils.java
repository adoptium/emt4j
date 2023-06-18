package org.eclipse.emt4j.common.util;

public class StringUtils {
    public static String stripDoubleQuote(String str) {
        return str.substring(str.indexOf('"') + 1, str.lastIndexOf('"'));
    }

    public static String readableRule(String rule) {
        return rule.replaceAll("\\$version", "Version should ")
                .replaceAll("\\$jar", "Jar name should ")
                .replaceAll(".ge", ">=")
                .replaceAll(".lt", "<")
                .replaceAll(".le", "<=")
                .replaceAll(".eq", "==")
                .replaceAll(".ne", "!=")
                .replaceAll(".gt", ">=")
                .replaceAll(".contains", "include")
                .replace('(', ' ')
                .replace(')', ' ');
    }

}
