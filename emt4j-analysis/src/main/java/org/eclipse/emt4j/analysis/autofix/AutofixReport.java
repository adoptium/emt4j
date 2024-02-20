/********************************************************************************
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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

import lombok.Data;
import org.eclipse.emt4j.analysis.autofix.recipe.RecipeFixReporter;
import org.eclipse.emt4j.analysis.autofix.recipe.ReportingRecipe;
import org.eclipse.emt4j.common.CheckResultContext;
import org.eclipse.emt4j.common.Feature;
import org.openrewrite.Recipe;
import org.openrewrite.SourceFile;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.eclipse.emt4j.analysis.report.render.AbstractRender.*;

public class AutofixReport {
    private static final AutofixReport instance = new AutofixReport();
    private Map<Feature, List<CheckResultContext>> fixedJavaAndDependency = new HashMap<>();
    private Map<Feature, List<CheckResultContext>> unFixedJavaAndDependency = new HashMap<>();
    private List<FixedInfo> otherFixedInfo = new ArrayList<>();

    private List<RecipeFixReporter> fixReporters = new ArrayList<>();

    // XXX:ugly hack
    // update or add dependency of some ga are not reported yet, we report them here
    private Map<String, RecipeFixReporter> ga2Reporter = new HashMap<>();

    private Map<String, Function<Recipe, RecipeFixReporter>> recipeName2ReporterGenerator = new HashMap<>();
    private Map<Recipe, RecipeFixReporter> reporterCache = new HashMap<>();

    @Data
    public static class FixedInfo implements Comparable<FixedInfo>{
        private String fixType;
        private String[] i18nParams;
        private int fixCount;
        private Collection<String> files;

        public FixedInfo(String fixType, String[] i18nParams, int fixCount, Collection<String> files) {
            this.fixType = fixType;
            this.i18nParams = i18nParams;
            this.fixCount = fixCount;
            this.files = files;
        }

        @Override
        public int compareTo(FixedInfo that) {
            int compareType = fixType.compareTo(that.fixType);
            if (compareType != 0 ){
                return compareType;
            }
            // as to same type, params length should be the same
            if (i18nParams == null) {
                return 0;
            }
            for (int i = 0; i < i18nParams.length; i++) {
                int compareParam = i18nParams[i].compareTo(that.i18nParams[i]);
                if (compareParam != 0) {
                    return compareParam;
                }
            }
            return 0;
        }
    }

    public static int getOtherCount(Collection<FixedInfo> infos) {
        if (infos == null) {
            return 0;
        }
        return infos.stream()
                .mapToInt(info -> info.fixCount)
                .sum();
    }

    private int getFixedCount() {
        return getJavaAndDependencyProblemCount(fixedJavaAndDependency) + getOtherCount(otherFixedInfo);
    }

    private int getUnfixedCount() {
        return getJavaAndDependencyProblemCount(unFixedJavaAndDependency);
    }

    public synchronized void addRecipeFixReporter(RecipeFixReporter fixReporter) {
        fixReporters.add(fixReporter);
    }

    public Map<Feature, List<CheckResultContext>> getFixedJavaAndDependency() {
        return fixedJavaAndDependency;
    }

    public Map<Feature, List<CheckResultContext>> getUnFixedJavaAndDependency() {
        return unFixedJavaAndDependency;
    }

    public void doAfterAutofixing() {
        otherFixedInfo = fixReporters.stream()
                .map(RecipeFixReporter::getFixedInfo)
                .filter(info -> info != null && info.getFixCount() != 0)
                .sorted()
                .collect(Collectors.toList());

        reporterCache.clear();
        recipeName2ReporterGenerator.clear();
        ga2Reporter.clear();
        fixReporters.clear();
    }

    public List<FixedInfo> getOtherFixedInfo() {
        return otherFixedInfo;
    }

    public List<FixedInfo> getOtherUnfixedInfo() {
        // currently we have no way to known where we can't fix
        return Collections.emptyList();
    }

    public void saveCheckResultMap(Map<Feature, List<CheckResultContext>> resultMap) {
        if (AutofixConfig.getInstance().isAutofix()) {
            BaseAutofixExecutor executor = BaseAutofixExecutor.getInstance();

            for (Map.Entry<Feature, List<CheckResultContext>> entry : resultMap.entrySet()) {
                Feature feature = entry.getKey();
                List<CheckResultContext> fixedList = new ArrayList<>();
                List<CheckResultContext> unfixedList = new ArrayList<>();

                for (CheckResultContext check : entry.getValue()) {
                    if (executor.isFixed(check)) {
                        fixedList.add(check);
                    } else {
                        unfixedList.add(check);
                    }
                }
                if (!fixedList.isEmpty()) {
                    fixedJavaAndDependency.put(feature, fixedList);
                }
                if (!unfixedList.isEmpty()) {
                    unFixedJavaAndDependency.put(feature, unfixedList);
                }
            }
        } else  {
            unFixedJavaAndDependency = resultMap;
        }
    }

    public static AutofixReport getInstance() {
        return instance;
    }

    public void addReporterForGA(String groupID, String artifactID, RecipeFixReporter reporter) {
        String ga = groupID + ":" + artifactID;
        ga2Reporter.put(ga, reporter);
    }

    public void reportChangeForGA(String groupID, String artifactID, SourceFile file) {
        if (groupID == null || artifactID == null) {
            return;
        }
        String ga = groupID + ":" + artifactID;
        if (ga2Reporter.containsKey(ga)) {
            RecipeFixReporter reporter = ga2Reporter.get(ga);
            reporter.recordModification(file);
        }
    }

    public void addRecipeReporterGenerator(String recipeName, Function<Recipe, RecipeFixReporter> reporterFunction) {
        recipeName2ReporterGenerator.put(recipeName, reporterFunction);
    }

    public RecipeFixReporter getRecipeReporter(Recipe recipe) {
        RecipeFixReporter reporter = null;
        if (reporterCache.containsKey(recipe)) {
            return reporterCache.get(recipe);
        }
        if (recipe instanceof ReportingRecipe) {
            reporter = ((ReportingRecipe)recipe).getReporter();
        } else if (recipeName2ReporterGenerator.containsKey(recipe.getName())) {
            reporter =  recipeName2ReporterGenerator.get(recipe.getName()).apply(recipe);
        }
        // also cache null reporter to boost future getting
        reporterCache.put(recipe, reporter);
        return reporter;
    }
}
