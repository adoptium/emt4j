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

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.tree.J;

import java.util.Arrays;
import java.util.Optional;


public class StreamCountRecipe extends Recipe implements ReportingRecipe {

    @Override
    public String getDisplayName() {
        return "Stream.count() Recipe";
    }

    @Override
    public String getDescription() {
        //language=markdown
        return "Convert Stream.count() to Stream.mapToLong(x->1).sum().";
    }

    @Override
    public RecipeFixReporter getReporter() {
        return new AbstractRecipeFixReporter.CountByFileRecipeFixReporter("autofix.java.streamCount");
    }

    private static final String[] STREAM_COUNT_CLASSES = new String[]{
            "java.util.stream.Stream",
            "java.util.stream.IntStream",
            "java.util.stream.LongStream",
            "java.util.stream.DoubleStream"
    };

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new JavaIsoVisitor<ExecutionContext>() {
            @Override
            public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext executionContext) {
                J.MethodInvocation invocation = super.visitMethodInvocation(method, executionContext);
                Optional<String> streamClass = Arrays.stream(STREAM_COUNT_CLASSES)
                        .filter(clazz -> new MethodMatcher(clazz + " count()").matches(invocation))
                        .findFirst();
                if (!streamClass.isPresent()) {
                    return invocation;
                }
                String mapMethod = "java.util.stream.LongStream".equals(streamClass.get()) ? "map" : "mapToLong";
                JavaTemplate t = JavaTemplate
                        .builder("#{any(" + streamClass.get() + ")}." + mapMethod + "(_ignore->1).sum()")
                        .build();
                return t.apply(updateCursor(invocation), invocation.getCoordinates().replace(), invocation.getSelect());
            }
        };
    }
}
