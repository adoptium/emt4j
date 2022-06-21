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

import org.eclipse.emt4j.common.rule.ExecutableRule;
import org.eclipse.emt4j.common.rule.model.CheckResult;
import org.eclipse.emt4j.common.rule.model.ConfRuleItem;
import org.eclipse.emt4j.common.rule.model.ConfRules;
import org.eclipse.emt4j.common.util.ClassUtil;
import org.eclipse.emt4j.common.util.FileUtil;
import org.eclipse.emt4j.common.util.JdkClassUtil;
import org.eclipse.emt4j.common.DependType;
import org.eclipse.emt4j.common.Dependency;
import org.eclipse.emt4j.common.RuleImpl;

import java.util.*;

/**
 * Generate the "--add-exports" option of JPMS
 * If reference a class that is not exported by default, and the class is not in java.base,
 * then need an explicit "--add-exports" to JVM option.
 */
@RuleImpl(type = "add-exports")
public class AddExportsRule extends ExecutableRule {

    private String packagesFile;
    private String moduleToPackageFile;
    private String defaultExportsFile;
    protected Map<String, String> exportPackageToModule = new HashMap<>();
    private Set<String> packageSet = new HashSet<>();

    /**
     * JDK 11 internal export all packages that exist in JDK 8 to unnamed
     * If the reference class's package is in the list, no need to add --add-exports.
     */
    protected Set<String> jdkDefaultExportToUnnamed = new HashSet<>();

    public AddExportsRule(ConfRuleItem confRuleItem, ConfRules confRules) {
        super(confRuleItem, confRules);
    }

    @Override
    public void init() {
        FileUtil.readPlainTextFromResource(confRules.getRuleDataPathPrefix() + moduleToPackageFile, false).forEach((l) -> {
            String[] arr = l.split(",");
            exportPackageToModule.put(arr[0].trim(), arr[1].trim());
        });
        packageSet.addAll(FileUtil.readPlainTextFromResource(confRules.getRuleDataPathPrefix() + packagesFile, false));
        jdkDefaultExportToUnnamed.addAll(FileUtil.readPlainTextFromResource(confRules.getRuleDataPathPrefix() + defaultExportsFile, false));
    }

    @Override
    public CheckResult check(Dependency dependency) {
        String className = dependency.getTarget().asClass().getClassName();
        if (!JdkClassUtil.isJdkClass(className)) {
            return CheckResult.PASS;
        }
        Optional<String> packageName = ClassUtil.getPackage(className);
        if (packageName.isPresent()) {

            //if the package that already export to unnamed,we no need --add-export again
            if (jdkDefaultExportToUnnamed.contains(packageName.get())) {
                return CheckResult.PASS;
            }

            String[] modulePackage = matchModule(packageName.get());
            if (modulePackage == null) {
                return CheckResult.PASS;
            } else {
                if ("java.base".equals(modulePackage[0]) || !packageSet.contains(modulePackage[1])) {
                    //java.base is default export to all module.so no need --add-exports
                    return CheckResult.PASS;
                } else {
                    return buildAddExportOption(modulePackage[0], modulePackage[1]);
                }
            }
        } else {
            return CheckResult.PASS;
        }
    }

    @Override
    public boolean accept(Dependency dependency) {
        return DependType.CLASS == dependency.getDependType();
    }

    private String[] matchModule(String packageName) {
        String currPackage = packageName;
        String module = exportPackageToModule.get(packageName);
        if (module == null) {
            //search to parent package until found module.
            int index = packageName.lastIndexOf('.');
            while (index != -1) {
                currPackage = packageName.substring(0, index);
                module = exportPackageToModule.get(currPackage);
                if (module != null) {
                    return new String[]{module, currPackage};
                }
                index = packageName.substring(0, index).lastIndexOf('.');
            }
            return null;
        } else {
            return new String[]{module, currPackage};
        }
    }

    private CheckResult buildAddExportOption(String sourceModule, String sourcePackage) {
        Map<String, Object> context = new HashMap<>();
        context.put("sourceModule", sourceModule);
        context.put("sourcePackage", sourcePackage);
        return CheckResult.fail(context);
    }

    public void setPackagesFile(String packagesFile) {
        this.packagesFile = packagesFile;
    }

    public void setModuleToPackageFile(String moduleToPackageFile) {
        this.moduleToPackageFile = moduleToPackageFile;
    }

    public void setDefaultExportsFile(String defaultExportsFile) {
        this.defaultExportsFile = defaultExportsFile;
    }
}