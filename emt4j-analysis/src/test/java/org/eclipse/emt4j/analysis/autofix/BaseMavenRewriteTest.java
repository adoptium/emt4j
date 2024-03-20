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

import org.eclipse.emt4j.analysis.autofix.recipe.MavenProjectMarker;
import org.openrewrite.Recipe;
import org.openrewrite.config.CompositeRecipe;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.SourceSpec;
import org.openrewrite.test.SourceSpecs;
import org.openrewrite.xml.XmlParser;
import org.openrewrite.xml.tree.Xml.Document;

import java.util.Collections;
import java.util.function.Consumer;

public abstract class BaseMavenRewriteTest implements RewriteTest {
    protected SourceSpecs pom(@Nullable String before) {
        return pom(before, s -> {
        });
    }

    protected SourceSpecs pom(@Nullable String before, Consumer<SourceSpec<Document>> spec) {
        SourceSpec<Document> pom = new SourceSpec<>(Document.class, null, XmlParser.builder(), before, null);
        pom.path("pom.xml");
        spec.andThen(file -> file.markers(new MavenProjectMarker(null))).accept(pom);
        return pom;
    }

    protected SourceSpecs pom(@Nullable String before, @Nullable String after) {
        return pom(before, after, s -> {
        });
    }

    protected SourceSpecs pom(@Nullable String before, @Nullable String after,
                              Consumer<SourceSpec<Document>> spec) {
        SourceSpec<Document> pom = new SourceSpec<>(Document.class, null, XmlParser.builder(), before, s -> after);
        pom.path("pom.xml");
        spec.andThen(file -> file.markers(new MavenProjectMarker(null))).accept(pom);
        return pom;
    }

    // HACK: Rewrite test requires the recipe to be deserializable. We can break this
    // restraint by wrapping the recipe with a CompositeRecipe.
    public static Recipe buildRecipe(Recipe recipe) {
        return new CompositeRecipe(Collections.singletonList(recipe));
    }
}
