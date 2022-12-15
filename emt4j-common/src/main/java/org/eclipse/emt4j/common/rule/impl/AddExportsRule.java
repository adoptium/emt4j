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
        Optional<String> packageName = ClassUtil.getPackage(className);
        if (!packageName.isPresent()) {
            return CheckResult.PASS;
        }

        if (packageName.get().startsWith("com.sun.proxy")) {
            return CheckResult.PASS;
        }

        if (matchModule(packageName.get()) != null) {
            // skip check for normal module classes now
            return CheckResult.PASS;
        }

        Set<String> typeSet = dependency.getClassSymbol().getTypeSet();
        Set<String> shouldExports = new HashSet<>();
        for (String type : typeSet) {
            packageName = ClassUtil.getPackage(type);
            if (!packageName.isPresent()) {
                continue;
            }
            if (jdkDefaultExportToUnnamed.contains(packageName.get())) {
                continue;
            }
            String[] modAndPkg = matchModule(packageName.get());
            if (modAndPkg == null) {
                continue;
            }
            if ("java.base".equals(modAndPkg[0]) || !packageSet.contains(modAndPkg[1])) {
                continue;
            }

            shouldExports.add(modAndPkg[0] + "/" + modAndPkg[1]);
        }

        if (shouldExports.isEmpty()) {
            return CheckResult.PASS;
        }

        Map<String, Object> context = new HashMap<>();
        context.put("shouldExports", shouldExports);
        // strip class load frames
        StackTraceElement[] stacktrace = dependency.getStacktrace();
        int index = -1;
        for (int i = stacktrace.length - 1; i >= 0; i--) {
            if("java.lang.ClassLoader".equals(stacktrace[i].getClassName()) && "loadClass".equals(stacktrace[i].getMethodName())) {
                index = i;
                break;
            }
            if("java.lang.Class".equals(stacktrace[i].getClassName()) && "forName".equals(stacktrace[i].getMethodName())) {
                index = i;
                break;
            }
        }
        if (index != -1) {
            dependency.setStacktrace(Arrays.copyOfRange(stacktrace, index + 1, stacktrace.length));
        }
        return CheckResult.fail(context);
    }

    @Override
    public boolean accept(Dependency dependency) {
        return DependType.WHOLE_CLASS== dependency.getDependType();
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