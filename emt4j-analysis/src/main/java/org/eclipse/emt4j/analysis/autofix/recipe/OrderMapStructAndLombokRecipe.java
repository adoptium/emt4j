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
package org.eclipse.emt4j.analysis.autofix.recipe;

import lombok.EqualsAndHashCode;
import org.eclipse.emt4j.analysis.autofix.XmlTagMatcher;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.xml.tree.Xml;

import java.util.Set;
import java.util.stream.Collectors;

import static org.eclipse.emt4j.analysis.common.Constant.POM_ARTIFACT_ID;
import static org.eclipse.emt4j.analysis.common.Constant.POM_GROUP_ID;


@EqualsAndHashCode(callSuper = true)
public class OrderMapStructAndLombokRecipe extends Recipe implements ReportingRecipe {

    private static final XmlTagMatcher before = tag -> "path" .equals(tag.getName())
            && XmlTagHelper.matchTag(tag, POM_GROUP_ID, "org.projectlombok")
            && XmlTagHelper.matchTag(tag, POM_ARTIFACT_ID, "lombok");

    private static final XmlTagMatcher after = tag -> "path" .equals(tag.getName())
            && XmlTagHelper.matchTag(tag, POM_GROUP_ID, "org.mapstruct")
            && XmlTagHelper.matchTag(tag, POM_ARTIFACT_ID, "mapstruct-processor");

    @Override
    public String getDisplayName() {
        return "Put mapstruct-processor after lombok in annotation processor";
    }

    @Override
    public String getDescription() {
        return getDisplayName();
    }

    public OrderMapStructAndLombokRecipe() {

    }

    @Override
    public RecipeFixReporter getReporter() {
        return new AbstractRecipeFixReporter.CountAsOneProblemRecipeFixReporter("autofix.pom.orderAnnotationProcessor", new String[]{"lombok", "mapstruct"});
    }

    /**
     * put mapstruct-processor after lombok
     * example:
     *
     * <plugin>
     *     <groupId>org.apache.maven.plugins</groupId>
     *     <artifactId>maven-compiler-plugin</artifactId>
     *     <version>3.8.1</version>
     *     <configuration>
     *         <encoding>UTF-8</encoding>
     *         <source>${maven.compiler.source}</source>
     *         <target>${maven.compiler.target}</target>
     *         <annotationProcessorPaths>
     *         		<path>
     *                 <groupId>org.projectlombok</groupId>
     *                 <artifactId>lombok</artifactId>
     *                 <version>${lombok.version}</version>
     *             </path>
     *             <path>
     *                 <groupId>org.mapstruct</groupId>
     *                 <artifactId>mapstruct-processor</artifactId>
     *                 <version>${mapstruct.version}</version>
     *             </path>
     *         </annotationProcessorPaths>
     *     </configuration>
     * </plugin>
     */

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new OrderMapStructAndLombokVisitor();
    }

    private static class OrderMapStructAndLombokVisitor extends MyMavenVisitor<ExecutionContext> {

        @Override
        public Xml visitDocument(Xml.Document document, ExecutionContext executionContext) {
            Set<Xml.Tag> mavenPlugins = XmlTagHelper.findTags(document, tag -> isPluginTag(tag)
                    && XmlTagHelper.matchTag(tag, POM_ARTIFACT_ID, "maven-compiler-plugin"));
            Set<Xml.Tag> annotationProcessorPaths = mavenPlugins.stream()
                    .flatMap(compiler -> XmlTagHelper.findDescendants(compiler, "/configuration/annotationProcessorPaths").stream())
                    .collect(Collectors.toSet());
            return new OrderTagVisitor(annotationProcessorPaths::contains, before, after).visitDocument(document, executionContext);
        }
    }
}
