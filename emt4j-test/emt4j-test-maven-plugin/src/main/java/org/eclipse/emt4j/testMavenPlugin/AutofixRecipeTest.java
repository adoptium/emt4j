/********************************************************************************
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
package org.eclipse.emt4j.testMavenPlugin;

import org.eclipse.emt4j.test.common.TestConf;

import java.util.Arrays;
import java.util.Collections;

import static org.eclipse.emt4j.test.common.TestConf.ModeEnum;

@TestConf(mode = {ModeEnum.MAVEN_PLUGIN}, option = "process-test-classes org.eclipse.emt4j:emt4j-maven-plugin:${version}:process -Dpriority=p1 -Dautofix=true -DfixedReportFileName=fixedReport.html -DunfixedReportFileName=unfixedReport.html -DautofixFile=fixed.patch -DoutputFile=report.html")
public class AutofixRecipeTest extends BaseMavenPluginSITCase {

    @Override
    protected String getTestProject() {
        return "recipeTest";
    }

    @Override
    protected void verify() {
        assertApplyFixedPatchSucceed("fixed.patch");
        String[] pom = getFileContent("pom.xml").split("\n");
        String[] subPom = getFileContent("sub/pom.xml").split("\n");
        // openrewrite will add such content to first line if there is unhandled
        // exception during executing recipes
        assertTrue(!pom[0].startsWith("<!--~~("));
        assertTrue(!subPom[0].startsWith("<!--~~("));

        checkPinyin4j(pom, subPom);
        checkNoImportStar();
        checkLombok(pom, subPom);
        checkJavax(pom);
        checkPlugins(pom, subPom);
        checkDependency(pom, subPom);
        checkExcludeJDKInternalRemovedJars(pom);
    }

    private void checkDependency(String[] pom, String[] subPom) {
        assertContainsInContinuousLines(pom, Arrays.asList(
                "<dependency>",
                "<groupId>org.apache.commons</groupId>",
                "<artifactId>commons-lang3</artifactId>",
                "<version>3.12.0</version>",
                "</dependency>"
        ));

        assertContainsInContinuousLines(pom, Arrays.asList(
                "<dependency>",
                "<groupId>org.mapstruct</groupId>",
                "<artifactId>mapstruct-processor</artifactId>",
                "<version>1.5.5.Final</version>",
                "</dependency>"
        ));

        assertContainsInContinuousLines(pom, Arrays.asList(
                "<dependency>",
                "<groupId>org.mapstruct</groupId>",
                "<artifactId>mapstruct</artifactId>",
                "<version>1.5.5.Final</version>",
                "</dependency>"
        ));

        assertTrue(!containsInContinuousLines(pom, Arrays.asList("mapstruct-jdk8")));
    }

    private void checkPinyin4j(String[] pom, String[] subPom) {
        assertContainsInContinuousLines(pom, Arrays.asList(
                "<dependency>",
                "<groupId>io.github.tokenjan</groupId>",
                "<artifactId>pinyin4j</artifactId>",
                "<version>2.6.1</version>",
                "</dependency>"
        ));
        assertContainsInContinuousLines(subPom, Arrays.asList(
                "<dependency>",
                "<groupId>io.github.tokenjan</groupId>",
                "<artifactId>pinyin4j</artifactId>",
                "</dependency>"
        ));
    }

    private void checkNoImportStar() {
        String noImportStar = getFileContent("sub/src/main/java/org/eclipse/emt4j/test/NoImportStar.java");
        assertTrue(noImportStar.contains("import java.util.Base64;"));
        assertTrue(!noImportStar.contains("import java.util.*;"));
    }

    private void checkLombok(String[] pom, String[] subPom) {
        assertContainsInContinuousLines(pom, Arrays.asList(
                "<dependency>",
                "<groupId>org.projectlombok</groupId>",
                "<artifactId>lombok</artifactId>",
                "<version>1.18.22</version>",
                "</dependency>"
        ));

        assertContainsInContinuousLines(pom, Arrays.asList(
                "<dependency>",
                "<groupId>org.projectlombok</groupId>",
                "<artifactId>lombok-mapstruct-binding</artifactId>",
                "<version>0.2.0</version>",
                "</dependency>"
        ));

        String lombokAnnotation = getFileContent("sub/src/main/java/org/eclipse/emt4j/test/LombokAnnotation.java");
        assertTrue(!lombokAnnotation.contains("lombok.experimental"));
        assertTrue(lombokAnnotation.contains("lombok.Builder"));
        assertTrue(lombokAnnotation.contains("lombok.Value"));
        assertTrue(lombokAnnotation.contains("lombok.With"));

        // consider property
        assertContainsInContinuousLines(pom, Collections.singletonList("<lombok.version>1.18.22</lombok.version>"));
        assertContainsInContinuousLines(subPom, Arrays.asList(
                "<dependency>",
                "<groupId>org.projectlombok</groupId>",
                "<artifactId>lombok</artifactId>",
                "<version>${lombok.version}</version>",
                "</dependency>"
        ));
    }

    private void checkJavax(String[] pom) {
        // add this dependency
        assertContainsInContinuousLines(pom, Arrays.asList(
                "<dependency>",
                "<groupId>javax.xml.bind</groupId>",
                "<artifactId>jaxb-api</artifactId>",
                "<version>2.3.0</version>",
                "</dependency>"
        ));
        assertContainsInContinuousLines(pom, Arrays.asList(
                "<dependency>",
                "<groupId>javax.xml.soap</groupId>",
                "<artifactId>javax.xml.soap-api</artifactId>",
                "<version>1.4.0</version>",
                "</dependency>"
        ));

        // should update version
        assertContainsInContinuousLines(pom, Arrays.asList(
                "<dependency>",
                "<groupId>com.sun.xml.bind</groupId>",
                "<artifactId>jaxb-core</artifactId>",
                "<version>2.3.0</version>",
                "</dependency>"
        ));

        // should be excluded
        assertContainsInContinuousLines(pom, Arrays.asList(
                "<dependency>",
                "<groupId>org.apache.maven</groupId>",
                "<artifactId>maven-aether-provider</artifactId>",
                "<version>",
                "<exclusions>",
                "<exclusion>",
                "<groupId>javax.annotation</groupId>",
                "<artifactId>jsr250-api</artifactId>",
                "</exclusion>",
                "</exclusions>",
                "</dependency>"
        ));

        // comment
        assertContainsInOrder(pom, Arrays.asList(
                "<!--JDK11 upgrade start-->",
                "<groupId>com.sun.xml.bind</groupId>",
                "<!--JDK11 upgrade end-->"
        ));
    }

    private void checkPlugins(String[] pom, String[] subPom) {
        assertContainsInContinuousLines(pom, Arrays.asList(
                "<plugin>",
                "<artifactId>maven-compiler-plugin</artifactId>",
                "<version>3.8.1</version>",
                "<configuration>"
        ));

        assertContainsInContinuousLines(pom, Arrays.asList(
                "<plugin>",
                "<groupId>org.apache.maven.plugins</groupId>",
                "<artifactId>maven-surefire-plugin</artifactId>",
                "<version>3.0.0-M5</version>",
                "<dependencies>",
                "<dependency>",
                "<groupId>org.apache.maven.surefire</groupId>",
                "<artifactId>surefire-junit47</artifactId>",
                "<version>3.0.0-M5</version>",
                "</dependency>",
                "</dependencies>",
                "</plugin>"
        ));

        // check recipe can correctly update property
        assertContainsInContinuousLines(pom, Collections.singletonList("<compiler_plugin.versionSub>3.10.1</compiler_plugin.versionSub>"));
        assertContainsInContinuousLines(subPom, Arrays.asList(
                "<plugin>",
                "<artifactId>maven-compiler-plugin</artifactId>",
                "<version>${compiler_plugin.versionSub}</version>",
                "</plugin>"
        ));

        assertContainsInContinuousLines(pom, Arrays.asList(
                "<annotationProcessorPaths>",
                "<path>",
                "<groupId>org.projectlombok</groupId>",
                "<artifactId>lombok</artifactId>",
                "<version>${lombok.version}</version>",
                "</path>",
                "<path>",
                "<groupId>org.mapstruct</groupId>",
                "<artifactId>mapstruct-processor</artifactId>",
                "<version>${mapstruct.version}</version>",
                "</path>",
                "</annotationProcessorPaths>"
        ));


        assertContainsInContinuousLines(pom, Arrays.asList(
                "<compilerArguments>",
                "<verbose/>",
                "</compilerArguments>"
        ));

        assertContainsInContinuousLines(pom, Collections.singletonList("<jacoco.version>0.8.8</jacoco.version>"));
        assertContainsInContinuousLines(pom, Arrays.asList(
                "<plugin>",
                "<groupId>org.jacoco</groupId>",
                "<artifactId>jacoco-maven-plugin</artifactId>",
                "<version>${jacoco.version}</version>",
                "</plugin>"
        ));

        assertContainsInContinuousLines(subPom, Collections.singletonList("<jacoco.versionSub>0.8.8</jacoco.versionSub>"));
        assertContainsInContinuousLines(subPom, Arrays.asList(
                "<plugin>",
                "<groupId>org.jacoco</groupId>",
                "<artifactId>jacoco-maven-plugin</artifactId>",
                "<version>${jacoco.versionSub}</version>",
                "</plugin>"
        ));
    }


    // Notice: this check may fail if JAVA_HOME is not set
    private void checkExcludeJDKInternalRemovedJars(String[] pom) {
        assertTrue(!containsInContinuousLines(pom, Arrays.asList(
                "<dependency>",
                "<groupId>com.sun</groupId>",
                "<artifactId>tools</artifactId>",
                "<version>",
                "<scope>system</scope>"
        )));

        assertTrue(!containsInContinuousLines(pom, Arrays.asList(
                "<dependency>",
                "<groupId>com.sun</groupId>",
                "<artifactId>rt</artifactId>",
                "<version>",
                "<scope>system</scope>"
        )));
    }
}
