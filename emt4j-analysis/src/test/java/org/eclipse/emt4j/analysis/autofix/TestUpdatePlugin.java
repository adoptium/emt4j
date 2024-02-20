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

import org.eclipse.emt4j.analysis.autofix.recipe.UpdatePluginVersion;
import org.junit.Test;
import org.openrewrite.test.RecipeSpec;

public class TestUpdatePlugin extends BaseMavenRewriteTest {
    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(buildRecipe(new UpdatePluginVersion("*", "jacoco-maven-plugin", "0.8.8")));
    }

    @Test
    public void updateBuildPlugin() {
        rewriteRun(
                pom("<project>\n" +
                        "    <groupId>org.eclipse.emt4j</groupId>\n" +
                        "    <artifactId>emt4j-test-maven-plugin-project</artifactId>\n" +
                        "    <version>1.0.0</version>\n" +
                        "    <build>\n" +
                        "        <plugins>\n" +
                        "            <plugin>\n" +
                        "                <groupId>org.jacoco</groupId>\n" +
                        "                <artifactId>jacoco-maven-plugin</artifactId>\n" +
                        "                <version>0.8.0</version>\n" +
                        "            </plugin>\n" +
                        "        </plugins>\n" +
                        "    </build>\n" +
                        "</project>", "<project>\n" +
                        "    <groupId>org.eclipse.emt4j</groupId>\n" +
                        "    <artifactId>emt4j-test-maven-plugin-project</artifactId>\n" +
                        "    <version>1.0.0</version>\n" +
                        "    <build>\n" +
                        "        <plugins>\n" +
                        "            <plugin>\n" +
                        "                <groupId>org.jacoco</groupId>\n" +
                        "                <artifactId>jacoco-maven-plugin</artifactId>\n" +
                        "                <version>0.8.8</version>\n" +
                        "            </plugin>\n" +
                        "        </plugins>\n" +
                        "    </build>\n" +
                        "</project>")
        );
    }

    @Test
    public void updateBuildPluginManagement() {
        rewriteRun(
                pom("<project>\n" +
                        "    <groupId>org.eclipse.emt4j</groupId>\n" +
                        "    <artifactId>emt4j-test-maven-plugin-project</artifactId>\n" +
                        "    <version>1.0.0</version>\n" +
                        "    <build>\n" +
                        "        <pluginManagement>\n" +
                        "            <plugins>\n" +
                        "                <plugin>\n" +
                        "                    <groupId>org.jacoco</groupId>\n" +
                        "                    <artifactId>jacoco-maven-plugin</artifactId>\n" +
                        "                    <version>0.8.0</version>\n" +
                        "                </plugin>\n" +
                        "            </plugins>\n" +
                        "        </pluginManagement>\n" +
                        "    </build>\n" +
                        "</project>", "<project>\n" +
                        "    <groupId>org.eclipse.emt4j</groupId>\n" +
                        "    <artifactId>emt4j-test-maven-plugin-project</artifactId>\n" +
                        "    <version>1.0.0</version>\n" +
                        "    <build>\n" +
                        "        <pluginManagement>\n" +
                        "            <plugins>\n" +
                        "                <plugin>\n" +
                        "                    <groupId>org.jacoco</groupId>\n" +
                        "                    <artifactId>jacoco-maven-plugin</artifactId>\n" +
                        "                    <version>0.8.8</version>\n" +
                        "                </plugin>\n" +
                        "            </plugins>\n" +
                        "        </pluginManagement>\n" +
                        "    </build>\n" +
                        "</project>")
        );
    }

    @Test
    public void updateProfileBuildPlugin() {
        rewriteRun(
                pom("<project>\n" +
                        "    <groupId>org.eclipse.emt4j</groupId>\n" +
                        "    <artifactId>emt4j-test-maven-plugin-project</artifactId>\n" +
                        "    <version>1.0.0</version>\n" +
                        "    <profiles>\n" +
                        "        <profile>\n" +
                        "            <id>test</id>\n" +
                        "            <build>\n" +
                        "                <plugins>\n" +
                        "                    <plugin>\n" +
                        "                        <groupId>org.jacoco</groupId>\n" +
                        "                        <artifactId>jacoco-maven-plugin</artifactId>\n" +
                        "                        <version>0.8.0</version>\n" +
                        "                    </plugin>\n" +
                        "                </plugins>\n" +
                        "            </build>\n" +
                        "        </profile>\n" +
                        "    </profiles>\n" +
                        "</project>", "<project>\n" +
                        "    <groupId>org.eclipse.emt4j</groupId>\n" +
                        "    <artifactId>emt4j-test-maven-plugin-project</artifactId>\n" +
                        "    <version>1.0.0</version>\n" +
                        "    <profiles>\n" +
                        "        <profile>\n" +
                        "            <id>test</id>\n" +
                        "            <build>\n" +
                        "                <plugins>\n" +
                        "                    <plugin>\n" +
                        "                        <groupId>org.jacoco</groupId>\n" +
                        "                        <artifactId>jacoco-maven-plugin</artifactId>\n" +
                        "                        <version>0.8.8</version>\n" +
                        "                    </plugin>\n" +
                        "                </plugins>\n" +
                        "            </build>\n" +
                        "        </profile>\n" +
                        "    </profiles>\n" +
                        "</project>")
        );
    }

    @Test
    public void noVersionRegression() {
        rewriteRun(
                pom("<project>\n" +
                        "    <groupId>org.eclipse.emt4j</groupId>\n" +
                        "    <artifactId>emt4j-test-maven-plugin-project</artifactId>\n" +
                        "    <version>1.0.0</version>\n" +
                        "    <build>\n" +
                        "        <pluginManagement>\n" +
                        "            <plugins>\n" +
                        "                <plugin>\n" +
                        "                    <groupId>org.jacoco</groupId>\n" +
                        "                    <artifactId>jacoco-maven-plugin</artifactId>\n" +
                        "                    <version>0.8.10</version>\n" +
                        "                </plugin>\n" +
                        "            </plugins>\n" +
                        "        </pluginManagement>\n" +
                        "    </build>\n" +
                        "</project>")
        );
    }
}
