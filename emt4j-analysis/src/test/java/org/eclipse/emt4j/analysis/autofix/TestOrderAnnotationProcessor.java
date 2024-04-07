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

import org.eclipse.emt4j.analysis.autofix.recipe.OrderMapStructAndLombokRecipe;
import org.junit.Test;
import org.openrewrite.test.RecipeSpec;

public class TestOrderAnnotationProcessor extends BaseMavenRewriteTest {
    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(buildRecipe(new OrderMapStructAndLombokRecipe()));
    }

    @Test
    public void orderTwoAnnotationProcessor() {
        rewriteRun(
                pom("<project>\n" +
                                "    <build>\n" +
                                "        <plugins>\n" +
                                "            <plugin>\n" +
                                "                <groupId>org.apache.maven.plugins</groupId>\n" +
                                "                <artifactId>maven-compiler-plugin</artifactId>\n" +
                                "                <version>3.8.1</version>\n" +
                                "                <configuration>\n" +
                                "                    <encoding>UTF-8</encoding>\n" +
                                "                    <source>${maven.compiler.source}</source>\n" +
                                "                    <target>${maven.compiler.target}</target>\n" +
                                "                    <annotationProcessorPaths>\n" +
                                "                        <path>\n" +
                                "                            <groupId>org.mapstruct</groupId>\n" +
                                "                            <artifactId>mapstruct-processor</artifactId>\n" +
                                "                            <version>${mapstruct.version}</version>\n" +
                                "                        </path>\n" +
                                "                        <path>\n" +
                                "                            <groupId>org.projectlombok</groupId>\n" +
                                "                            <artifactId>lombok</artifactId>\n" +
                                "                            <version>${lombok.version}</version>\n" +
                                "                        </path>\n" +
                                "                    </annotationProcessorPaths>\n" +
                                "                </configuration>\n" +
                                "            </plugin>\n" +
                                "        </plugins>\n" +
                                "    </build>\n" +
                                "</project>\n",

                        "<project>\n" +
                                "    <build>\n" +
                                "        <plugins>\n" +
                                "            <plugin>\n" +
                                "                <groupId>org.apache.maven.plugins</groupId>\n" +
                                "                <artifactId>maven-compiler-plugin</artifactId>\n" +
                                "                <version>3.8.1</version>\n" +
                                "                <configuration>\n" +
                                "                    <encoding>UTF-8</encoding>\n" +
                                "                    <source>${maven.compiler.source}</source>\n" +
                                "                    <target>${maven.compiler.target}</target>\n" +
                                "                    <annotationProcessorPaths>\n" +
                                "                        <path>\n" +
                                "                            <groupId>org.projectlombok</groupId>\n" +
                                "                            <artifactId>lombok</artifactId>\n" +
                                "                            <version>${lombok.version}</version>\n" +
                                "                        </path>\n" +
                                "                        <path>\n" +
                                "                            <groupId>org.mapstruct</groupId>\n" +
                                "                            <artifactId>mapstruct-processor</artifactId>\n" +
                                "                            <version>${mapstruct.version}</version>\n" +
                                "                        </path>\n" +
                                "                    </annotationProcessorPaths>\n" +
                                "                </configuration>\n" +
                                "            </plugin>\n" +
                                "        </plugins>\n" +
                                "    </build>\n" +
                                "</project>\n"
                )
        );
    }
}
