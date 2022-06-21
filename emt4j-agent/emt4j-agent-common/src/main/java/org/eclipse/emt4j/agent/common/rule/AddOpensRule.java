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
package org.eclipse.emt4j.agent.common.rule;

import org.eclipse.emt4j.agent.common.methodvisitor.TransformerFactory;
import org.eclipse.emt4j.agent.common.rule.accessible.SetInstanceAccessibleMethodVisitor;
import org.eclipse.emt4j.agent.common.rule.accessible.SetStaticAccessibleMethodVisitor;
import org.eclipse.emt4j.common.DependType;
import org.eclipse.emt4j.common.Dependency;
import org.eclipse.emt4j.common.MethodDesc;
import org.eclipse.emt4j.common.RuleImpl;
import org.eclipse.emt4j.common.rule.ExecutableRule;
import org.eclipse.emt4j.common.rule.model.CheckResult;
import org.eclipse.emt4j.common.rule.model.ConfRuleItem;
import org.eclipse.emt4j.common.rule.model.ConfRules;
import org.eclipse.emt4j.common.util.ClassUtil;
import org.eclipse.emt4j.common.util.FileUtil;
import org.eclipse.emt4j.common.util.JdkClassUtil;

import java.util.*;

/**
 * <p>
 * JPMS require add "--add-opens=${module}/package" option when running java.
 * But getting private field is difficult. Programmer can get the field by different ways,
 * then may pass the Field to other method, track all the fields is impossible.
 * </p>
 * But if a private field or a private method, it need call the "setAccessible" method,
 * so we transform the "java/lang/reflect/AccessibleObject.setAccessible" method.
 */
@RuleImpl(type = "add-opens")
public class AddOpensRule extends ExecutableRule {
    private String packagesFile;
    private String moduleToPackageFile;
    protected Map<String, String> exportPackageToModule = new HashMap<>();
    private Set<String> packageSet = new HashSet<>();
    private MethodDesc[] callMethods = new MethodDesc[]{
            new MethodDesc("java/lang/reflect/AccessibleObject", "java.lang.reflect.AccessibleObject", "setAccessible", "(Z)V"),
            new MethodDesc("java/lang/reflect/AccessibleObject", "java.lang.reflect.AccessibleObject", "setAccessible", "([Ljava/lang/reflect/AccessibleObject;Z)V")
    };

    public AddOpensRule(ConfRuleItem confRuleItem, ConfRules confRules) {
        super(confRuleItem, confRules);
    }

    @Override
    public void init() {
        FileUtil.readPlainTextFromResource(confRules.getRuleDataPathPrefix() + moduleToPackageFile, false).forEach((l) -> {
            String[] arr = l.split(",");
            exportPackageToModule.put(arr[0].trim(), arr[1].trim());
        });
        packageSet.addAll(FileUtil.readPlainTextFromResource(confRules.getRuleDataPathPrefix() + packagesFile, false));
        TransformerFactory.register(callMethods[0], (mvp) -> new SetInstanceAccessibleMethodVisitor(mvp));
        TransformerFactory.register(callMethods[1], (mvp) -> new SetStaticAccessibleMethodVisitor(mvp));
    }

    @Override
    public CheckResult check(Dependency dependency) {
        if (!JdkClassUtil.isJdkClass(dependency.getTarget().asClass().getClassName())) {
            return CheckResult.PASS;
        }
        Optional<String> packageName = ClassUtil.getPackage(dependency.getTarget().asClass().getClassName());
        if (packageName.isPresent()) {
            String[] modulePackage = matchModule(packageName.get());
            if (modulePackage != null && packageSet.contains(modulePackage[1])) {
                return buildAddOpenOption(modulePackage[0], modulePackage[1]);
            } else {
                return CheckResult.PASS;
            }
        } else {
            return CheckResult.PASS;
        }
    }

    private CheckResult buildAddOpenOption(String sourceModule, String sourcePackage) {
        Map<String, Object> context = new HashMap<>();
        context.put("sourceModule", sourceModule);
        context.put("sourcePackage", sourcePackage);
        return CheckResult.fail(context);
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

    @Override
    public boolean accept(Dependency dependency) {
        return DependType.METHOD_TO_CLASS_DEEP_REFLECTION == dependency.getDependType();
    }

    public void setPackagesFile(String packagesFile) {
        this.packagesFile = packagesFile;
    }

    public void setModuleToPackageFile(String moduleToPackageFile) {
        this.moduleToPackageFile = moduleToPackageFile;
    }
}