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

import org.eclipse.emt4j.analysis.autofix.recipe.OrderPluginRecipe;
import org.junit.Test;
import org.openrewrite.test.RecipeSpec;

public class TestOrderPlugin extends BaseMavenRewriteTest {
    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(buildRecipe(new OrderPluginRecipe("a1", "a2")));
    }

    @Test
    public void orderTwoPlugins() {
        rewriteRun(
                pom("<project>\n" +
                                "    <build>\n" +
                                "        <plugins>\n" +
                                "            <plugin>\n" +
                                "                <groupId>g2</groupId>\n" +
                                "                <artifactId>a2</artifactId>\n" +
                                "            </plugin>\n" +
                                "            <plugin>\n" +
                                "                <artifactId>a1</artifactId>\n" +
                                "            </plugin>\n" +
                                "        </plugins>\n" +
                                "    </build>\n" +
                                "</project>",

                        "<project>\n" +
                                "    <build>\n" +
                                "        <plugins>\n" +
                                "            <plugin>\n" +
                                "                <artifactId>a1</artifactId>\n" +
                                "            </plugin>\n" +
                                "            <plugin>\n" +
                                "                <groupId>g2</groupId>\n" +
                                "                <artifactId>a2</artifactId>\n" +
                                "            </plugin>\n" +
                                "        </plugins>\n" +
                                "    </build>\n" +
                                "</project>"
                )
        );
    }

    @Test
    public void alreadyOrdered() {
        rewriteRun(
                pom("<project>\n" +
                        "    <build>\n" +
                        "        <plugins>\n" +
                        "            <plugin>\n" +
                        "                <artifactId>a1</artifactId>\n" +
                        "            </plugin>\n" +
                        "            <plugin>\n" +
                        "                <groupId>g2</groupId>\n" +
                        "                <artifactId>a2</artifactId>\n" +
                        "            </plugin>\n" +
                        "        </plugins>\n" +
                        "    </build>\n" +
                        "</project>"
                )
        );
    }
}
