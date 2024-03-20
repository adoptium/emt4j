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
package org.eclipse.emt4j.analysis.autofix;

import org.eclipse.emt4j.analysis.autofix.recipe.UpdatePluginDependencyVersion;
import org.eclipse.emt4j.analysis.autofix.recipe.UpdatePluginVersion;
import org.junit.Test;
import org.openrewrite.test.RecipeSpec;

public class TestUpdatePluginDependency extends BaseMavenRewriteTest {
    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(buildRecipe(new UpdatePluginDependencyVersion("*", "maven-surefire-plugin",
                "org.apache.maven.surefire", "surefire-junit47", "3.0.0-M5")));
    }

    @Test
    public void updateDependency() {
        rewriteRun(
                pom("<project>\n" +
                                "    <groupId>org.eclipse.emt4j</groupId>\n" +
                                "    <artifactId>emt4j-test-maven-plugin-project</artifactId>\n" +
                                "    <version>1.0.0</version>\n" +
                                "    <build>\n" +
                                "        <plugins>\n" +
                                "            <plugin>\n" +
                                "                <groupId>org.apache.maven.plugins</groupId>\n" +
                                "                <artifactId>maven-surefire-plugin</artifactId>\n" +
                                "                <version>2.20.1</version>\n" +
                                "                <dependencies>\n" +
                                "                    <dependency>\n" +
                                "                        <groupId>org.apache.maven.surefire</groupId>\n" +
                                "                        <artifactId>surefire-junit47</artifactId>\n" +
                                "                        <version>2.19.1</version>\n" +
                                "                    </dependency>\n" +
                                "                </dependencies>\n" +
                                "                <configuration>\n" +
                                "                    <forkCount>1</forkCount>\n" +
                                "                    <reuseForks>true</reuseForks>\n" +
                                "                </configuration>\n" +
                                "            </plugin>\n" +
                                "        </plugins>\n" +
                                "    </build>\n" +
                                "</project>",

                        "<project>\n" +
                                "    <groupId>org.eclipse.emt4j</groupId>\n" +
                                "    <artifactId>emt4j-test-maven-plugin-project</artifactId>\n" +
                                "    <version>1.0.0</version>\n" +
                                "    <build>\n" +
                                "        <plugins>\n" +
                                "            <plugin>\n" +
                                "                <groupId>org.apache.maven.plugins</groupId>\n" +
                                "                <artifactId>maven-surefire-plugin</artifactId>\n" +
                                "                <version>2.20.1</version>\n" +
                                "                <dependencies>\n" +
                                "                    <dependency>\n" +
                                "                        <groupId>org.apache.maven.surefire</groupId>\n" +
                                "                        <artifactId>surefire-junit47</artifactId>\n" +
                                "                        <version>3.0.0-M5</version>\n" +
                                "                    </dependency>\n" +
                                "                </dependencies>\n" +
                                "                <configuration>\n" +
                                "                    <forkCount>1</forkCount>\n" +
                                "                    <reuseForks>true</reuseForks>\n" +
                                "                </configuration>\n" +
                                "            </plugin>\n" +
                                "        </plugins>\n" +
                                "    </build>\n" +
                                "</project>")
        );
    }

    @Test
    public void noChange() {
        rewriteRun(
                pom("<project>\n" +
                        "    <groupId>org.eclipse.emt4j</groupId>\n" +
                        "    <artifactId>emt4j-test-maven-plugin-project</artifactId>\n" +
                        "    <version>1.0.0</version>\n" +
                        "    <dependencies>\n" +
                        "        <dependency>\n" +
                        "            <groupId>org.apache.maven.surefire</groupId>\n" +
                        "            <artifactId>surefire-junit47</artifactId>\n" +
                        "            <version>2.19.1</version>\n" +
                        "        </dependency>\n" +
                        "    </dependencies>\n" +
                        "    <build>\n" +
                        "        <plugins>\n" +
                        "            <plugin>\n" +
                        "                <groupId>org.apache.maven.plugins</groupId>\n" +
                        "                <artifactId>maven-surefire-plugin</artifactId>\n" +
                        "                <version>2.20.1</version>\n" +
                        "                <configuration>\n" +
                        "                    <forkCount>1</forkCount>\n" +
                        "                    <reuseForks>true</reuseForks>\n" +
                        "                </configuration>\n" +
                        "            </plugin>\n" +
                        "        </plugins>\n" +
                        "    </build>\n" +
                        "</project>")
        );
    }
}
