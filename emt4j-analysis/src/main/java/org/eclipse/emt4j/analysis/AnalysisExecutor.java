/********************************************************************************
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
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
package org.eclipse.emt4j.analysis;

import org.eclipse.emt4j.analysis.common.util.Progress;
import org.eclipse.emt4j.analysis.source.DependencySource;
import org.eclipse.emt4j.common.CheckConfig;
import org.eclipse.emt4j.common.DependType;
import org.eclipse.emt4j.common.Dependency;
import org.eclipse.emt4j.common.Feature;
import org.eclipse.emt4j.common.rule.ExecutableRule;
import org.eclipse.emt4j.common.rule.InstanceRuleManager;
import org.eclipse.emt4j.common.rule.model.ReportCheckResult;
import org.eclipse.emt4j.common.util.ClassURL;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Pipeline abstract the process to
 * <ul>
 *     <li>Source: Analysis dependency</li>
 *     <li>Execute Rule</li>
 *     <li>Accumulate check result</li>
 *     <li>Render the result to file</li>
 * </ul>
 */
public class AnalysisExecutor {
    private List<DependencySource> sourceList = new ArrayList<>();
    private AnalysisOutputConsumer analysisOutputConsumer;

    private CheckConfig checkConfig;

    public AnalysisExecutor(CheckConfig checkConfig) {
        this.checkConfig = checkConfig;
    }

    public void setAnalysisOutputConsumer(AnalysisOutputConsumer analysisOutputConsumer) {
        this.analysisOutputConsumer = analysisOutputConsumer;
    }

    public void add(DependencySource source) {
        sourceList.add(source);
    }

    public boolean hasSource() {
        return !sourceList.isEmpty();
    }

    /**
     * <ul>
     *     <li>Initialize the rule list</li>
     *     <li>Analysis all dependencies, then call the rule for each </li>
     *     <li>Generate the report </li>
     * </ul>
     *
     * @param featureList
     * @param parentProgress
     * @throws IOException
     */
    public void execute(List<Feature> featureList, Progress parentProgress) throws IOException {
        log("[Begin]Analysis");
        ClassURL.registerUrlProtocolHandler();
        InstanceRuleManager.init(RULE_CLASS, new ArrayList<>(featureList).toArray(new Feature[featureList.size()]),
                new String[]{"class", "source"},
                checkConfig.getFromVersion(), checkConfig.getToVersion(), checkConfig.getPriority());
        analysisOutputConsumer.onBegin(checkConfig, featureList);

        Set<Integer> alreadyChecked = new ConcurrentSkipListSet<>();

        sourceList.parallelStream().forEach(
                source -> {
                    try {
                        source.parse((d) -> {
                            try {
                                int hashCode = d.hashCode();
                                if (alreadyChecked.contains(hashCode) || !alreadyChecked.add(hashCode)) {
                                    return;
                                }
                                for (ExecutableRule rule : InstanceRuleManager.getRuleInstanceList()) {
                                    if (rule.shouldAccept(d)) {
                                        ReportCheckResult checkResult = rule.execute(d);
                                        if (!checkResult.isPass()) {
                                            if (checkResult.getPropagated().isEmpty()) {
                                                analysisOutputConsumer.onNewRecord(d, checkResult, rule, source.getInformation());
                                            } else {
                                                for (Dependency newDependency : checkResult.getPropagated()) {
                                                    analysisOutputConsumer.onNewRecord(newDependency, checkResult, rule, source.getInformation());
                                                }
                                            }
                                        }
                                    }
                                }
                                if (d.getDependType() == DependType.CODE_SOURCE || d.getDependType() == DependType.VM_OPTION) {
                                    analysisOutputConsumer.onNewRecord(d, null, null, source.getInformation());
                                }
                            } catch (Throwable t) {
                                System.err.println("Failed to analyze " + source.getFile().getName());
                                t.printStackTrace();
                            }
                        }, null);
                        syncPrint("  Analyze " + source.getFile().getName() + " done");
                    } catch (Throwable t) {
                        System.err.println("Failed to analyze " + source.getFile().getName());
                        t.printStackTrace();
                    }
                }
        );
        log("[End]Analysis");
        log("Done!");
    }

    private void syncPrint(String msg) {
        synchronized (this) {
            System.out.println(msg);
        }
    }

    private void log(String msg) {
        if (checkConfig.isVerbose()) {
            System.out.println(msg);
        }
    }

    private static String[] RULE_CLASS = new String[]{
            "org.eclipse.emt4j.common.rule.impl.AddExportsRule",
            "org.eclipse.emt4j.common.rule.impl.IncompatibleJarRule",
            "org.eclipse.emt4j.common.rule.impl.JvmOptionRule",
            "org.eclipse.emt4j.common.rule.impl.ReferenceClassRule",
            "org.eclipse.emt4j.common.rule.impl.TouchedMethodRule",
            "org.eclipse.emt4j.common.rule.impl.WholeClassRule",
            "org.eclipse.emt4j.common.rule.impl.DeprecatedAPIRule",
    };
}
