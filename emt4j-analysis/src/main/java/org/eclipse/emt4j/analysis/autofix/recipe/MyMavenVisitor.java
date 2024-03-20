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

import org.apache.maven.project.MavenProject;
import org.eclipse.emt4j.analysis.autofix.MavenHelper;
import org.eclipse.emt4j.analysis.autofix.Version;
import org.openrewrite.SourceFile;
import org.openrewrite.xml.ChangeTagValueVisitor;
import org.openrewrite.xml.XPathMatcher;
import org.openrewrite.xml.XmlVisitor;
import org.openrewrite.xml.tree.Xml;

import java.util.Optional;

import static org.eclipse.emt4j.analysis.common.Constant.*;

public abstract class MyMavenVisitor<P> extends XmlVisitor<P> {
    private MavenProject project;

    private Xml.Document document;
    static final XPathMatcher DEPENDENCY_MATCHER = new XPathMatcher("/project/dependencies/dependency");
    static final XPathMatcher MANAGED_DEPENDENCY_MATCHER = new XPathMatcher("/project/dependencyManagement/dependencies/dependency");
    static final XPathMatcher PROPERTY_MATCHER = new XPathMatcher("/project/properties/*");
    static final XPathMatcher PLUGIN_MATCHER = new XPathMatcher("/project/*/plugins/plugin");
    static final XPathMatcher PLUGIN_IN_MANAGEMENT_MATCHER = new XPathMatcher("/project/*/pluginManagement/plugins/plugin");
    static final XPathMatcher PARENT_MATCHER = new XPathMatcher("/project/parent");
    static final XPathMatcher MODULE_SELF_MATCHER = new XPathMatcher("/project");

    private static XPathMatcher DEPENDENCIES_MATCHER = new XPathMatcher("/project/dependencies");
    private static final XPathMatcher MANAGED_DEPENDENCIES_MATCHER = new XPathMatcher("/project/dependencyManagement/dependencies");

    @Override
    public boolean isAcceptable(SourceFile sourceFile, P p) {
        return super.isAcceptable(sourceFile, p) && getMavenMarker(sourceFile) != null;
    }

    public boolean isPropertyTag() {
        return PROPERTY_MATCHER.matches(getCursor());
    }

    public boolean isDependencyTag() {
        return DEPENDENCY_MATCHER.matches(getCursor());
    }

    public boolean isManagedDependencyTag() {
        return MANAGED_DEPENDENCY_MATCHER.matches(getCursor());
    }

    public boolean isPluginTag() {
        return getCursor().getPathAsStream()
                          .filter(p -> p instanceof Xml.Tag)
                          .findFirst()
                          .map(Xml.Tag.class::cast)
                          .map(t -> POM_PLUGIN.equals(t.getName()))
                          .orElse(false);
    }

    public static boolean isPluginTag(Xml.Tag tag) {
        return POM_PLUGIN.equals(tag.getName());
    }

    public static boolean isPluginsTag(Xml.Tag tag) {
        return POM_PLUGINS.equals(tag.getName());
    }

    public boolean isParentTag() {
        return PARENT_MATCHER.matches(getCursor());
    }

    public boolean isModuleSelfTag() {
        return MODULE_SELF_MATCHER.matches(getCursor());
    }

    public boolean isDependenciesTag() {
        return DEPENDENCIES_MATCHER.matches(getCursor());
    }

    public MavenProjectMarker getMavenMarker(SourceFile sourceFile) {
        return sourceFile.getMarkers().findFirst(MavenProjectMarker.class).orElse(null);
    }

    // should only be used in visitDocument override, because project field is not ready
    protected MavenProject getMavenProject(SourceFile sourceFile) {
        return getMavenMarker(sourceFile).getProject();
    }

    public MavenProject getMavenProject() {
        return project;
    }

    public boolean isManagedDependenciesTag() {
        return MANAGED_DEPENDENCIES_MATCHER.matches(getCursor());
    }

    // Update child tag <version> if it is available. Return true if any modification is made
    protected boolean updatePossiblyExistVersionTag(Xml.Tag tag, String targetVersion) {
        Optional<Xml.Tag> optionalVersionTag = tag.getChild(POM_VERSION);
        if (optionalVersionTag.isPresent()) {
            // currently we don't support changing tag in different pom
            Xml.Tag tagThatWillBeChanged = MavenHelper.findTagRecursivelyThatDefinesValue(project, optionalVersionTag.get(), true);
            Xml.Tag tagThatDefinedValue = MavenHelper.findTagRecursivelyThatDefinesValue(project, optionalVersionTag.get(), false);
            String currentVersion = tagThatDefinedValue.getValue().orElse("");
            if (currentVersion.startsWith("${") || new Version(currentVersion).shouldUpdateTo(new Version(targetVersion))) {
                doAfterVisit(new ChangeTagValueVisitor<>(tagThatWillBeChanged, targetVersion));
                return true;
            }
        }
        return false;
    }

    protected Xml.Document currentDocument() {
        return document;
    }

    @Override
    public Xml visitDocument(Xml.Document document, P p) {
        project = getMavenProject(document);
        this.document = document;
        return super.visitDocument(document, p);
    }
}
