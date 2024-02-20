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

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.rtinfo.RuntimeInformation;
import org.apache.maven.settings.crypto.SettingsDecrypter;
import org.eclipse.emt4j.analysis.autofix.DependencyUpdateRule.*;
import org.eclipse.emt4j.analysis.autofix.recipe.*;
import org.openrewrite.*;
import org.openrewrite.config.Environment;
import org.openrewrite.java.migrate.CastArraysAsListToList;
import org.openrewrite.java.migrate.UseJavaUtilBase64;

import java.util.*;


// This executor contains most recipes that we support
public class FullAutofixExecutor extends BaseAutofixExecutor {
    public static final String[] JAVAEE_DEPENDENCIES = new String[]{
            "com.sun.activation:javax.activation:jar:1.2.0",
            "javax.transaction:javax.transaction-api:jar:1.2",
            "javax.xml.bind:jaxb-api:jar:2.3.0",
            "javax.xml.soap:javax.xml.soap-api:jar:1.4.0",
            "com.sun.xml.bind:jaxb-core:jar:2.3.0",
            "com.sun.xml.bind:jaxb-impl:jar:2.3.0",
            "com.sun.xml.ws:jaxws-ri:pom:2.3.3",
            "javax.annotation:javax.annotation-api:jar:1.3.2",
    };

    private static final DependencyUpdateRule[] FAMOUS_DEPENDENCY_UPDATE_RULES = {
            new GATChangeDependencyUpdateRule("net.sourceforge.pinyin4j:pinyin4j:jar", "io.github.tokenjan:pinyin4j:jar:2.6.1"),
            new GADontChangeDependencyUpdateRule("org.projectlombok:lombok:jar:1.18.22"),
            new GADontChangeDependencyUpdateRule("org.apache.commons:commons-lang3:jar:3.12.0"),
            new GADontChangeDependencyUpdateRule("org.asynchttpclient:async-http-client:jar:2.12.3"),
            new GATChangeDependencyUpdateRule("org.mapstruct:mapstruct-jdk8:jar", "org.mapstruct:mapstruct:jar:1.5.5.Final"),
            new GADontChangeDependencyUpdateRule("org.mapstruct:mapstruct-processor:jar:1.5.5.Final"),
            new GADontChangeDependencyUpdateRule("commons-lang:commons-lang:jar:2.6"),

    };

    public FullAutofixExecutor(AutofixConfig config, MavenSession session, MavenProject project, Log log, RuntimeInformation runtime, SettingsDecrypter settingsDecrypter) {
        super(config, session, project, log, runtime, settingsDecrypter);
    }

    @Override
    protected boolean needPrescanSourceFiles() {
        return true;
    }

    private PomUpdatePlan generatePomUpdatePlan() {
        PomUpdatePlanGenerator pomUpdateGenerator = new PomUpdatePlanGenerator();

        // update dependencies that we know they should be updated
        for (DependencyUpdateRule rule : FAMOUS_DEPENDENCY_UPDATE_RULES) {
            pomUpdateGenerator.updateDependency(rule, true);
        }

        // javaee packages
        for (String dependency : JAVAEE_DEPENDENCIES) {
            pomUpdateGenerator.addDependency(dependency);
        }
        // we need this to decide if a check can be autofixed
        setFromGatv2toGatv(pomUpdateGenerator.getFromGatv2toGatv());

        return pomUpdateGenerator.generatePlan();
    }

    @Override
    protected Recipe getRecipe(Environment env) {
        AutofixReport report = AutofixReport.getInstance();
        List<String> recipesNames = new ArrayList<>();
        List<Recipe> recipes = new ArrayList<>();

        if (config.getToVersion() == 11) {
            recipesNames.add("org.eclipse.emt4j.analysis.autofix.recipe.UpdateLombokExperimental");
            report.addRecipeReporterGenerator("org.eclipse.emt4j.analysis.autofix.recipe.UpdateLombokExperimental",
                    (recipe) -> new AbstractRecipeFixReporter.CountByFileRecipeFixReporter("autofix.java.lombokExperimental"));
            recipes.add(new MigrateSecurityManagerCheck());
            recipes.add(new TernaryUnboxingGenericRecipe());
            recipes.add(new CastArraysAsListToList());
            recipes.add(new UseJavaUtilBase64("sun.misc", true));
        } else if (config.getToVersion() == 17) {
            recipesNames.add("org.openrewrite.java.migrate.Java11toJava17");
        } else {
            throw new RuntimeException("Unsupported target version: " + config.getToVersion());
        }

        PomUpdatePlan pomUpdatePlan = generatePomUpdatePlan();

        recipes.add(new OrderMapStructAndLombokRecipe());
        recipes.add(new PomUpdateRecipe(pomUpdatePlan));
        recipes.add(new UpdatePluginVersion("*", "maven-compiler-plugin", "3.8.1"));
        recipes.add(new RemoveCompilerPluginCompilerArg("bootclasspath"));
        recipes.add(new UpdatePluginVersion("*", "maven-surefire-plugin", "3.0.0-M5"));
        recipes.add(new UpdatePluginDependencyVersion("*", "maven-surefire-plugin",
                "org.apache.maven.surefire", "surefire-junit47", "3.0.0-M5"));
        recipes.add(new UpdatePluginVersion("*", "jacoco-maven-plugin", "0.8.8"));
        recipes.addAll(new DependencyExclusionRecipeMaker().getDependencyExclusionRecipes());
        recipes.add(generateAddJDKMigrationCommentRecipe(pomUpdatePlan));

        Recipe recipe = env.activateRecipes(recipesNames);
        recipe.getRecipeList().addAll(recipes);
        return recipe;
    }

    private Recipe generateAddJDKMigrationCommentRecipe(PomUpdatePlan pomUpdatePlan) {
        List<String> dependencies = new ArrayList<>(Arrays.asList(JAVAEE_DEPENDENCIES));
        return new AddJDKMigrationCommentRecipe(dependencies, pomUpdatePlan);
    }
}
