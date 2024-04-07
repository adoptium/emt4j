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

import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import org.apache.maven.project.MavenProject;
import org.apache.maven.rtinfo.RuntimeInformation;
import org.apache.maven.settings.crypto.SettingsDecrypter;
import org.apache.maven.shared.utils.logging.MessageBuilder;
import org.apache.maven.shared.utils.logging.MessageUtils;
import org.eclipse.emt4j.analysis.autofix.recipe.*;
import org.eclipse.emt4j.analysis.common.util.Progress;
import org.eclipse.emt4j.analysis.report.render.AbstractRender;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Predicate;

import org.eclipse.emt4j.common.CheckResultContext;
import org.eclipse.emt4j.common.DependTarget;
import org.eclipse.emt4j.common.Feature;
import org.openrewrite.*;
import org.openrewrite.config.ClasspathScanningLoader;
import org.openrewrite.config.Environment;
import org.openrewrite.internal.InMemoryLargeSourceSet;
import org.openrewrite.java.tree.JavaSourceFile;
import org.openrewrite.marker.Generated;
import org.openrewrite.marker.Marker;
import org.openrewrite.marker.RecipesThatMadeChanges;
import org.openrewrite.maven.MavenMojoProjectParser;
import org.openrewrite.maven.SanitizedMarkerPrinter;
import org.openrewrite.style.NamedStyles;
import org.openrewrite.xml.tree.Xml;

import static java.util.stream.Collectors.toList;


// According to current design, Autofix can only be called from Maven plugin.
@Data
public abstract class BaseAutofixExecutor {
    private static BaseAutofixExecutor instance = null;
    protected AutofixConfig config;
    private MavenSession session;

    private MavenProject project;

    private Log log;

    protected RuntimeInformation runtime;

    protected SettingsDecrypter settingsDecrypter;

    private Map<Feature, List<CheckResultContext>> resultMap;

    // used to decide if an issue can be autofixed
    private Map<String, String> fromGatv2toGatv = new HashMap<>();

    private List<Predicate<CheckResultContext>> JAVA_CAN_BE_AUTOFIXED_PREDICATES = Arrays.asList(
            checkResultContext -> "ARRAYS_AS_LIST_TO_ARRAY".equals(checkResultContext.getReportCheckResult().getResultCode()),

            checkResultContext -> "REMOVE_CLASS".equals(checkResultContext.getReportCheckResult().getResultCode())
                    && checkResultContext.getDependency().getTarget() instanceof DependTarget.Class
                    && checkResultContext.getDependency().getTarget().asClass().getClassName().contains("sun.misc.BASE64"),

            checkResultContext -> "REMOVED_API".equals(checkResultContext.getReportCheckResult().getResultCode())
                    && checkResultContext.getDependency().getTarget() instanceof DependTarget.Method
                    && checkResultContext.getDependency().getTarget().asMethod().getClassName().equals("java.lang.SecurityManager")
                    && (checkResultContext.getDependency().getTarget().asMethod().getMethodName().equals("checkAwtEventQueueAccess")
                    || checkResultContext.getDependency().getTarget().asMethod().getMethodName().equals("checkSystemClipboardAccess"))
    );

    public BaseAutofixExecutor(AutofixConfig config, MavenSession session, MavenProject project, Log log, RuntimeInformation runtime, SettingsDecrypter settingsDecrypter) {
        this.config = config;
        this.session = session;
        this.project = project;
        this.log = log;
        this.runtime = runtime;
        this.settingsDecrypter = settingsDecrypter;
    }

    // following protected getXXX methods are used for subclasses to extend functionality

    protected abstract Recipe getRecipe(Environment env);

    protected Recipe getPrescanRecipe(Environment env) {
        return new PomScanRecipe();
    }


    // Currently we don't try to autofix these files so we exclude them for better performance.
    protected List<String> getSourceFileExclusions() {
        List<String> masks = new ArrayList<>();
        masks.add("**/*.jar");
        masks.add("**/*.class");
        masks.add("**/*.so");
        masks.add("**/*.json");
        masks.add("**/*.kt");
        return masks;
    }

    protected List<NamedStyles> getCodeStyles(Environment env) {
        List<String> styles = new ArrayList<>();
        styles.add("org.eclipse.emt4j.analysis.autofix.style");
        return env.activateStyles(styles);
    }

    protected boolean needPrescanSourceFiles() {
        return true;
    }

    protected List<String> getPlainTextMasks() {
        List<String> masks = new ArrayList<>();
        masks.add("**/Dockerfile*");
        masks.add("**/setenv.sh");
        return masks;
    }


    public void doAutofix(Map<Feature, List<CheckResultContext>> resultMap, Progress progress)
            throws DependencyResolutionRequiredException, MojoExecutionException {
        this.resultMap = resultMap;
        if (config.isAutofix()) {
            generatePatch(progress);
        }
    }

    private void generatePatch(Progress progress) throws DependencyResolutionRequiredException, MojoExecutionException {
        Progress autofixProgress = new Progress(progress, "Autofix");
        autofixProgress.printTitle();
        new Progress(autofixProgress, "Setting up environment").printTitle();
        ClasspathScanningLoader classpathScanningLoader = new ClasspathScanningLoader(project.getProperties(), this.getClass().getClassLoader());
        Environment.Builder eb = Environment.builder(project.getProperties());
        eb.load(classpathScanningLoader);
        Environment env = eb.build();
        ExecutionContext ec = new InMemoryExecutionContext(t -> {
            getLog().warn(t.getMessage());
            getLog().debug(t);
        });

        new Progress(autofixProgress, "Parse source files").printTitle();
        List<SourceFile> sourceFiles = parseSourceFiles(env, ec);

        if (needPrescanSourceFiles()) {
            new Progress(autofixProgress, "Prescan source files").printTitle();
            getPrescanRecipe(env).run(new InMemoryLargeSourceSet(sourceFiles), ec);
        }

        new Progress(autofixProgress, "Prepare recipes").printTitle();
        Recipe recipe = getRecipe(env);

        new Progress(autofixProgress, "Apply recipe to source files").printTitle();
        List<ResultAndDiff> results = recipe.run(new InMemoryLargeSourceSet(sourceFiles), ec)
                .getChangeset()
                .getAllResults()
                .stream()
                .filter(source -> {
                    if (source.getBefore() == null || source.getAfter() == null) {
                        return false;
                    }
                    return !source.getBefore().getMarkers().findFirst(Generated.class).isPresent();
                })
                .map((result -> new ResultAndDiff(result, result.diff())))
                .filter(resultAndDiff -> !resultAndDiff.diff.isEmpty())
                .peek(resultAndDiff -> reportResult(resultAndDiff.result))
                .collect(toList());

        new Progress(autofixProgress, "Write patch to file").printTitle();
        if (config.isAutofixGeneratePatch()) {
            writeAutofixPatch(results);
        } else {
            writeResultToFileDirectly(results);
        }
        AutofixReport.getInstance().doAfterAutofixing();
    }

    private void reportResult(Result result) {
        AutofixReport report = AutofixReport.getInstance();
        Optional<Marker> recipeStacks = result.getAfter().getMarkers().getMarkers().stream()
                .filter(marker -> marker instanceof RecipesThatMadeChanges).findFirst();
        if (!recipeStacks.isPresent()) {
            return;
        }
        for (List<Recipe> recipeStack : ((RecipesThatMadeChanges) recipeStacks.get()).getRecipes()) {
            for (Recipe recipe : recipeStack) {
                RecipeFixReporter reporter = report.getRecipeReporter(recipe);
                if (reporter != null) {
                    reporter.recordModification(result.getAfter());
                }
            }
        }
    }

    // notice: pom.xml will be parsed as normal xml file rather than maven project file. We should not use MavenVisitor
    // of OpenRewrite to deal with pom. Use MyMavenVisitor instead.
    private List<SourceFile> parseSourceFiles(Environment env, ExecutionContext ec) throws DependencyResolutionRequiredException, MojoExecutionException {
        Path baseDir = Paths.get(session.getExecutionRootDirectory()).toAbsolutePath();

        List<NamedStyles> styles = getCodeStyles(env);
        List<SourceFile> sourceFiles = new ArrayList<>();
        // please keep skipMavenParsing true, otherwise it will hang
        MavenMojoProjectParser projectParser = new MavenMojoProjectParser(getLog(), baseDir, true, null, runtime, true, getSourceFileExclusions(), getPlainTextMasks(), 64, session, settingsDecrypter, true, true);
        for (MavenProject projectIndex : session.getProjects()) {
            List<SourceFile> files = projectParser.listSourceFiles(projectIndex, styles, ec).collect(toList());
            for (SourceFile file : files) {
                sourceFiles.add(dealWithSourceFile(file));
            }
        }
        return sourceFiles;
    }

    private SourceFile dealWithSourceFile(SourceFile file) {
        if (file instanceof Xml) {
            MavenProject project = MavenHelper.getProjectFromPom(file);
            if (project != null) {
                file = file.withMarkers(file.getMarkers().add(new MavenProjectMarker(project)));
            }
        } else if (file instanceof JavaSourceFile) {
            MavenProjectLocalData data = MavenHelper.getDataFromProjectFile(file);
            if (data != null) {
                data.setJavaSourceCount(1 + data.getJavaSourceCount());
            }
        }
        return file;
    }

    private void writeAutofixPatch(List<ResultAndDiff> results) {
        for (ResultAndDiff resultAndDiff : results) {
            Result result = resultAndDiff.result;
            Charset charset = result.getAfter().getCharset();
            if (charset != null && charset != StandardCharsets.UTF_8) {
                throw new IllegalStateException("Can not generate autofix patch because encoding of " + result.getAfter().getSourcePath().toFile().getName() +
                        " file is not UTF-8. Please ensure all files in the project are encoded in UTF-8, and then try again.");
            }
        }

        if (results.isEmpty()) {
            MessageBuilder message = MessageUtils.buffer().strong("Autofix patch is not generated because nothing can be autofixed. ");
            getLog().info(message.toString());
        } else {
            StringBuilder content = new StringBuilder();
            for (ResultAndDiff resultAndDiff : results) {
                content.append(resultAndDiff.diff);
                content.append("\n");
            }

            System.out.println("patch length: " + content.length());
            System.out.println("patch content:");
            System.out.println(content.toString());

            try (OutputStreamWriter writer = new OutputStreamWriter(Files.newOutputStream(Paths.get(config.getAutofixFile())), StandardCharsets.UTF_8)) {
                writer.write(content.toString());
                AbstractRender.doLogGeneratedFilePath("Autofix patch", config.getAutofixFile());
            } catch (Exception e) {
                throw new RuntimeException("Unable to generate autofix file.", e);
            }
        }
    }

    private void writeResultToFileDirectly(List<ResultAndDiff> results) {
        for (int i = 0; i < results.size(); i++) {
            Result result = results.get(i).result;
            assert result.getAfter() != null;
            Charset charset = result.getAfter().getCharset() == null ? StandardCharsets.UTF_8 : result.getAfter().getCharset();
            try (BufferedWriter sourceFileWriter = Files.newBufferedWriter(result.getAfter().getSourcePath(), charset)) {
                sourceFileWriter.write(result.getAfter().printAll(new PrintOutputCapture<>(0, new SanitizedMarkerPrinter())));
            } catch (IOException e) {
                // rollback changes
                for (int rollBackIndex = 0; rollBackIndex <= i; rollBackIndex++) {
                    result = results.get(rollBackIndex).result;
                    assert result.getBefore() != null;
                    charset = result.getBefore().getCharset() == null ? StandardCharsets.UTF_8 : result.getBefore().getCharset();
                    try (BufferedWriter sourceFileWriter = Files.newBufferedWriter(result.getBefore().getSourcePath(), charset)) {
                        sourceFileWriter.write(result.getBefore().printAll(new PrintOutputCapture<>(0, new SanitizedMarkerPrinter())));
                    } catch (IOException rollbackError) {
                        throw new UncheckedIOException("Unable to rewrite source files and unable to rollback changes", e);
                    }
                }
                throw new UncheckedIOException("Unable to rewrite source files", e);
            }
        }
    }

    public static void setInstance(BaseAutofixExecutor instance) {
        BaseAutofixExecutor.instance = instance;
    }

    public static BaseAutofixExecutor getInstance() {
        return instance;
    }

    public boolean canBeAutofixed(CheckResultContext context) {
        if (context.getDependency().getSourceInformation().isDependency()) {
            String gatv = context.getDependency().buildDependencyGATV();
            return fromGatv2toGatv.containsKey(gatv);
        } else {
            return JAVA_CAN_BE_AUTOFIXED_PREDICATES.stream().anyMatch(predicate -> predicate.test(context));
        }
    }

    public boolean isFixed(CheckResultContext context) {
        if (config.isAutofix()) {
            return canBeAutofixed(context);
        } else {
            return false;
        }
    }

    @AllArgsConstructor
    private static class ResultAndDiff {
        Result result;
        String diff;
    }
}
