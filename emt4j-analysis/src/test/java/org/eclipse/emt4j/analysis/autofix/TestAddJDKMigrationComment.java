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

import org.eclipse.emt4j.analysis.autofix.recipe.AddJDKMigrationCommentRecipe;
import org.junit.Test;
import org.openrewrite.test.RecipeSpec;

import java.util.Arrays;

public class TestAddJDKMigrationComment extends BaseMavenRewriteTest {
    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(buildRecipe(new AddJDKMigrationCommentRecipe(Arrays.asList("g2:a2:t2:v2", "g3:a3:t3:v3"), null)));
    }

    @Test
    public void addComment() {
        rewriteRun(
                pom("<project>\n" +
                                "    <dependencies>\n" +
                                "        <dependency>\n" +
                                "            <groupId>g1</groupId>\n" +
                                "            <artifactId>a1</artifactId>\n" +
                                "            <version>v1</version>\n" +
                                "        </dependency>\n" +
                                "        <dependency>\n" +
                                "            <groupId>g2</groupId>\n" +
                                "            <artifactId>a2</artifactId>\n" +
                                "            <version>v2</version>\n" +
                                "        </dependency>\n" +
                                "        <dependency>\n" +
                                "            <groupId>g3</groupId>\n" +
                                "            <artifactId>a3</artifactId>\n" +
                                "            <version>v3</version>\n" +
                                "        </dependency>\n" +
                                "        <dependency>\n" +
                                "            <groupId>g4</groupId>\n" +
                                "            <artifactId>a4</artifactId>\n" +
                                "            <version>v4</version>\n" +
                                "        </dependency>\n" +
                                "    </dependencies>\n" +
                                "</project>\n",

                        "<project>\n" +
                                "    <dependencies>\n" +
                                "        <dependency>\n" +
                                "            <groupId>g1</groupId>\n" +
                                "            <artifactId>a1</artifactId>\n" +
                                "            <version>v1</version>\n" +
                                "        </dependency>\n" +
                                "        <!--JDK11 upgrade start-->\n" +
                                "        <dependency>\n" +
                                "            <groupId>g2</groupId>\n" +
                                "            <artifactId>a2</artifactId>\n" +
                                "            <version>v2</version>\n" +
                                "        </dependency>\n" +
                                "        <dependency>\n" +
                                "            <groupId>g3</groupId>\n" +
                                "            <artifactId>a3</artifactId>\n" +
                                "            <version>v3</version>\n" +
                                "        </dependency>\n" +
                                "        <!--JDK11 upgrade end-->\n" +
                                "        <dependency>\n" +
                                "            <groupId>g4</groupId>\n" +
                                "            <artifactId>a4</artifactId>\n" +
                                "            <version>v4</version>\n" +
                                "        </dependency>\n" +
                                "    </dependencies>\n" +
                                "</project>\n"
                )
        );
    }
}
