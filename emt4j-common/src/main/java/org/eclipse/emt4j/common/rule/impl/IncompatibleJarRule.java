/********************************************************************************
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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
package org.eclipse.emt4j.common.rule.impl;

import org.eclipse.emt4j.common.DependType;
import org.eclipse.emt4j.common.Dependency;
import org.eclipse.emt4j.common.JdkMigrationException;
import org.eclipse.emt4j.common.RuleImpl;
import org.eclipse.emt4j.common.rule.ExecutableRule;
import org.eclipse.emt4j.common.rule.model.*;
import org.eclipse.emt4j.common.util.FileUtil;
import org.eclipse.emt4j.common.util.JarFileInfoUtil;
import org.mvel2.MVEL;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.eclipse.emt4j.common.util.StringUtils.readableRule;
import static org.eclipse.emt4j.common.util.StringUtils.stripDoubleQuote;

/**
 * There are some jars that contain the incompatible problems in advance.
 * Try to match the input jars for all these rules.
 */
@RuleImpl(type = "incompatible-jar")
public class IncompatibleJarRule extends ExecutableRule {
    private String jarRuleFile;
    private Map<String, JarRule> sortArtifactToJarRule = new HashMap<>();

    public IncompatibleJarRule(ConfRuleItem confRuleItem, ConfRules confRules) {
        super(confRuleItem, confRules);
    }

    @Override
    public CheckResult check(Dependency dependency) {
        //the jar file name has no specification, we try to guess the artifact id and version from the jar
        //filename.
        Optional<JarFileInfo> jarFileInfo = JarFileInfoUtil.match(dependency.getTarget().asLocation().getLocationExternalForm());
        if (jarFileInfo.isPresent()) {
            JarRule jarRule = sortArtifactToJarRule.get(key(jarFileInfo.get().getOrderedArtifactFragments()));
            if (null == jarRule) {
                return CheckResult.PASS;
            }

            Map<String, Object> mvelMap = new HashMap<>();
            mvelMap.put("$version", new Version(jarFileInfo.get().getVersion()));
            mvelMap.put("$jar", new JarFileName(jarFileInfo.get().getJarFileName()));
            Object result = MVEL.eval(jarRule.getRule(), mvelMap);
            if (result instanceof Boolean) {
                if ((Boolean) result) {
                    return CheckResult.PASS;
                } else {
                    Map<String, Object> context = new HashMap<>();
                    context.put("jar", dependency.getTarget().asLocation().getLocationExternalForm());
                    context.put("rule", readableRule(jarRule.getRule()));
                    context.put("artifact", jarRule.getArtifact().toUpperCase());
                    return CheckResult.fail(context);
                }
            } else {
                throw new JdkMigrationException("MVEL expression not return boolean for artifact:" + jarRule.getArtifact() + ",rule:" + jarRule.getRule());
            }
        } else {
            return CheckResult.PASS;
        }
    }

    @Override
    public void init() {
        FileUtil.readPlainTextFromResource(confRules.getRuleDataPathPrefix() + jarRuleFile, false).forEach((l) -> {
            String[] artifactRule = l.split(",");
            String[] artifacts = stripDoubleQuote(artifactRule[0]).split("\\|");
            String rule = stripDoubleQuote(artifactRule[1]);
            for (String artifact : artifacts) {
                sortArtifactToJarRule.put(key(JarFileInfoUtil.sortArtifactFragments(artifact)), new JarRule(artifact, rule));
            }
        });
    }

    @Override
    public boolean accept(Dependency dependency) {
        return DependType.CODE_SOURCE == dependency.getDependType();
    }

    public String getJarRuleFile() {
        return jarRuleFile;
    }

    public void setJarRuleFile(String jarRuleFile) {
        this.jarRuleFile = jarRuleFile;
    }

    private String key(String[] sortArtifactFragments) {
        return String.join("-", sortArtifactFragments);
    }
}

