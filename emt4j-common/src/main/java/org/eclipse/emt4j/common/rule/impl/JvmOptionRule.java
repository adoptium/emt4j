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

import org.eclipse.emt4j.common.Dependency;
import org.eclipse.emt4j.common.JdkMigrationException;
import org.eclipse.emt4j.common.RuleImpl;
import org.eclipse.emt4j.common.rule.ExecutableRule;
import org.eclipse.emt4j.common.rule.model.CheckResult;
import org.eclipse.emt4j.common.rule.model.ConfRuleItem;
import org.eclipse.emt4j.common.rule.model.ConfRules;
import org.eclipse.emt4j.common.rule.model.VmOptionItem;
import org.eclipse.emt4j.common.util.FileUtil;
import org.eclipse.emt4j.common.DependType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Check the JVM option difference.
 */
@RuleImpl(type = "jvm-option")
public class JvmOptionRule extends ExecutableRule {
    private String jvmOptionFile;
    private Map<String, VmOptionItem> deprecatedVmOptionMap;

    public JvmOptionRule(ConfRuleItem confRuleItem, ConfRules confRules) {
        super(confRuleItem, confRules);
    }

    @Override
    public CheckResult check(Dependency dependency) {
        String[] givenOptions = dependency.getTarget().asVMOption().getVmOption().split("\\s+");
        List<String> expired = new ArrayList<>();
        List<String> obsoleted = new ArrayList<>();
        List<String> deprecated = new ArrayList<>();
        for (String givenOption : givenOptions) {
            //use fuzzy match to avoid analyze different option style
            containAny(givenOption, (foundOption) -> {
                if (foundOption.getExpiredVersion() != null && betweenInVersion(foundOption.getExpiredVersion())) {
                    expired.add(givenOption);
                } else if (foundOption.getObsoleteVersion() != null && betweenInVersion(foundOption.getObsoleteVersion())) {
                    obsoleted.add(givenOption);
                } else if (foundOption.getDeprecatedVersion() != null && betweenInVersion((foundOption.getDeprecatedVersion()))) {
                    deprecated.add(givenOption);
                } else {
                    expired.add(givenOption);
                }
            });
        }
        if (expired.isEmpty() && obsoleted.isEmpty() && deprecated.isEmpty()) {
            return CheckResult.PASS;
        } else {
            Map<String, Object> map = new HashMap<>();
            map.put("expired", expired);
            map.put("obsoleted", obsoleted);
            map.put("deprecated", deprecated);
            return CheckResult.fail(map);
        }
    }

    private void containAny(String givenOption, Consumer<VmOptionItem> foundConsumer) {
        deprecatedVmOptionMap.forEach((k, v) -> {
            if (givenOption.contains(k)) {
                foundConsumer.accept(v);
            }
        });
    }

    @Override
    public void init() {
        deprecatedVmOptionMap = new HashMap<>();
        List<String> lines = FileUtil.readPlainTextFromResource(confRules.getRuleDataPathPrefix() + jvmOptionFile, false);

        int lineNo = 0;
        for (String line : lines) {
            lineNo++;
            String[] arr = line.split(",");
            checkFormat(arr, line, lineNo);
            VmOptionItem vmSpecialOption = new VmOptionItem();
            if (arr.length >= 2) {
                vmSpecialOption.setDeprecatedVersion(convert(arr[1]));
            }
            if (arr.length >= 3) {
                vmSpecialOption.setObsoleteVersion(convert(arr[2]));
            }
            if (arr.length >= 4) {
                vmSpecialOption.setExpiredVersion(convert(arr[3]));
            }
            if (arr.length >= 5) {
                vmSpecialOption.setSuggestion(emptyIfDefault(arr[4]));
            }
            deprecatedVmOptionMap.put(arr[0], vmSpecialOption);
        }
    }

    @Override
    public boolean accept(Dependency dependency) {
        return DependType.VM_OPTION == dependency.getDependType();
    }


    public boolean betweenInVersion(int currentVersion) {
        return currentVersion >= confRules.getFromVersion() && currentVersion <= confRules.getToVersion();
    }

    public String getJvmOptionFile() {
        return jvmOptionFile;
    }

    public void setJvmOptionFile(String jvmOptionFile) {
        this.jvmOptionFile = jvmOptionFile;
    }

    private String emptyIfDefault(String str) {
        return "-".equals(str) ? "" : str;
    }

    private Integer convert(String origin) {
        String trimmed = origin.trim();
        return "-".equals(trimmed) ? null : Integer.valueOf(trimmed);
    }

    private void checkFormat(String[] arr, String line, int lineNo) {
        if (arr.length == 0) {
            throw new JdkMigrationException("Content at line  " + lineNo + " is malformed!content is : " + line);
        }
        for (String col : arr) {
            if (col == null || "".equals(col)) {
                throw new JdkMigrationException("Content at line " + lineNo + " is malformed!content is : " + line);
            }
        }
    }
}
