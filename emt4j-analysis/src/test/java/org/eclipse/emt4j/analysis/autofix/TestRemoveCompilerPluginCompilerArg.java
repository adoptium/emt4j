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

import org.eclipse.emt4j.analysis.autofix.recipe.RemoveCompilerPluginCompilerArg;
import org.junit.Test;
import org.openrewrite.test.RecipeSpec;

public class TestRemoveCompilerPluginCompilerArg extends BaseMavenRewriteTest {
    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(buildRecipe(new RemoveCompilerPluginCompilerArg("bootclasspath")));
    }

    @Test
    public void orderTwoPlugins() {
        rewriteRun(
                pom("<project>\n" +
                                "    <build>\n" +
                                "        <plugins>\n" +
                                "            <plugin>\n" +
                                "                <artifactId>maven-compiler-plugin</artifactId>\n" +
                                "                <version>2.5.1</version>\n" +
                                "                <configuration>\n" +
                                "                    <source>1.8</source>\n" +
                                "                    <target>1.8</target>\n" +
                                "                    <compilerArguments>\n" +
                                "                        <verbose/>\n" +
                                "                        <bootclasspath>\n" +
                                "                            ${java.home}/lib/rt.jar${path.separator}${java.home}/lib/jce.jar${path.separator}${java.home}/../Classes/classes.jar\n" +
                                "                        </bootclasspath>\n" +
                                "                    </compilerArguments>\n" +
                                "                </configuration>\n" +
                                "            </plugin>\n" +
                                "        </plugins>\n" +
                                "    </build>\n" +
                                "</project>",

                        "<project>\n" +
                                "    <build>\n" +
                                "        <plugins>\n" +
                                "            <plugin>\n" +
                                "                <artifactId>maven-compiler-plugin</artifactId>\n" +
                                "                <version>2.5.1</version>\n" +
                                "                <configuration>\n" +
                                "                    <source>1.8</source>\n" +
                                "                    <target>1.8</target>\n" +
                                "                    <compilerArguments>\n" +
                                "                        <verbose/>\n" +
                                "                    </compilerArguments>\n" +
                                "                </configuration>\n" +
                                "            </plugin>\n" +
                                "        </plugins>\n" +
                                "    </build>\n" +
                                "</project>"
                )
        );
    }
}
