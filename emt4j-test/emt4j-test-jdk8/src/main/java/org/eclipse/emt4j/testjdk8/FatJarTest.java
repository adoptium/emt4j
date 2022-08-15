package org.eclipse.emt4j.testjdk8;

import org.eclipse.emt4j.common.JsonReport;
import org.eclipse.emt4j.test.common.*;

import java.io.IOException;

@TestConf(mode = {TestConf.ModeEnum.DYNAMIC}, from = TestConf.RELEASE.JDK8, to = TestConf.RELEASE.JDK11)
public class FatJarTest extends SITBaseCase {
    @Override
    public void verify(JsonReport jsonReport) {
        assertTrue(matchAny(jsonReport, "GET_JDK_VERSION"));
        assertTrue(matchAny(jsonReport, "REMOVED_API"));
        assertTrue(matchAny(jsonReport, "DEPRECATED_API"));
    }

    @Override
    public void run() throws Exception {
    }

    @Override
    public void prepareDynamicTestTarget(String workDir) throws IOException {
        JavaSource[] aSources = new JavaSource[]{
                new JavaSource("com.a.GetVersion", "package com.a; public class GetVersion {    public String get() {\n" +
                        "        String javaVersion = System.getProperty(\"java.version\");\n" +
                        "        return javaVersion;\n" +
                        "    }\n" +
                        " }")
        };

        JavaSource[] bSources = new JavaSource[]{
                new JavaSource("com.b.RemoveAPI", "package com.b;public class RemoveAPI {\n" +
                        "    public void run() throws NoSuchMethodException {\n" +
                        "        Runtime.runFinalizersOnExit(true);\n" +
                        "    }" +
                        "}")
        };
        JavaSource[] cSources = new JavaSource[]{
                new JavaSource("com.m.M1", "public class M1", null, null, new JavaSource.MethodDesc[]{
                        new JavaSource.MethodDesc("name", "public String name() { return \"M1\"; }")
                }),
                new JavaSource("com.m.Main", "public class Main", null, null, new JavaSource.MethodDesc[]{
                        new JavaSource.MethodDesc("main", "    public static void main(String[] args) throws Exception {\n" +
                                "        M1 m1 = new M1();\n" +
                                "        System.out.println(m1.name());\n" +
                                "        java.io.File f = new java.io.File(System.getProperty(\"java.io.tmpdir\"));\n" +
                                "        f.toURL();\n" +
                                "    }")
                })
        };

        Project project = new Project(new RunFatJarConf("com.m.Main"),
                new Artifact[]{
                        Artifact.createFatJar("all-in-one", "fat-lib", "m-fat.jar",
                                new Artifact[]{
                                        Artifact.createPlainJar("a", "lib", "a.jar", null, aSources),
                                        Artifact.createPlainJar("b", "lib", "b.jar", null, bSources),
                                        Artifact.createClasses("c", "myclasses", null, cSources)
                                })
                });
        project.build(new ProjectWorkDir(workDir));
    }
}
