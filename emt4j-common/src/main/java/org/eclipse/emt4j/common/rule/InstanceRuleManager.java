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
package org.eclipse.emt4j.common.rule;

import org.eclipse.emt4j.common.JdkMigrationException;
import org.eclipse.emt4j.common.rule.model.ConfRuleItem;
import org.eclipse.emt4j.common.rule.model.ConfRules;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Create ExecutableRule instances by the given feature.
 * It should be called when application startup, then get rule by call getRuleInstanceList() method,
 * then call each rule.
 */
public class InstanceRuleManager {
    private static volatile boolean hasInit = false;
    private static List<ExecutableRule> ruleInstanceList = null;

    /**
     * load the rule, then create the rule instance.
     * After creating an instance, set the user-defined field's value.
     *
     * @param classList   All rule implementation classes
     * @param features
     * @param modes
     * @param fromVersion
     * @param toVersion
     */
    public synchronized static void init(String[] classList, String[] features, String[] modes, int fromVersion, int toVersion) {
        if (hasInit) {
            return;
        }
        try {
            List<ExecutableRule> instanceList = new ArrayList<>();
            List<ConfRules> confRulesList = ConfRuleFacade.load(features, modes, fromVersion, toVersion);
            Map<String, Class> ruleMap = RuleSelector.select(classList);
            for (ConfRules confRules : confRulesList) {
                for (ConfRuleItem ruleItem : confRules.getRuleItems()) {
                    Class c = ruleMap.get(ruleItem.getType());
                    if (null == c) {
                        throw new JdkMigrationException("Cannot found rule implementation for type : " + ruleItem.getType());
                    }
                    Constructor<ExecutableRule> constructor = c.getConstructor(ConfRuleItem.class, ConfRules.class);
                    if (null == constructor) {
                        throw new JdkMigrationException("The class: " + c.getName() + " is not a valid implementation of ExecutableRule!");
                    }
                    ExecutableRule executableRule = constructor.newInstance(ruleItem, confRules);
                    //inject value defined in rule config file to object instance.
                    if (ruleItem.getUserDefineAttrs() != null) {
                        for (String[] nameValue : ruleItem.getUserDefineAttrs()) {
                            setValue(executableRule, nameValue[0], nameValue[1]);
                        }
                    }
                    executableRule.init();
                    instanceList.add(executableRule);
                }
            }
            ruleInstanceList = Collections.unmodifiableList(instanceList);
            hasInit = true;
        } catch (Exception e) {
            throw new JdkMigrationException("InstanceRuleManager init exception!", e);
        }
    }

    private static void setValue(ExecutableRule executableRule, String attrName, String attrValue) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method m = executableRule.getClass().getMethod(getSetMethodName(attrName), String.class);
        m.setAccessible(true);
        m.invoke(executableRule, attrValue);
    }

    /**
     * try to convert an attribute name to a set method name
     * attribute        set method name
     * -------------------------------------
     * filename         setFilename
     * file-name        setFileName
     *
     * @param attrName
     * @return
     */
    private static String getSetMethodName(String attrName) {
        StringBuilder setMethodName = new StringBuilder(attrName.length() + "set".length());
        setMethodName.append("set");
        char[] chars = attrName.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '-') {
                continue;
            } else {
                if (i == 0 || (i != 0 && chars[i - 1] == '-')) {
                    setMethodName.append(Character.toUpperCase(chars[i]));
                } else {
                    setMethodName.append(chars[i]);
                }
            }
        }
        return setMethodName.toString();
    }

    public static List<ExecutableRule> getRuleInstanceList() {
        return ruleInstanceList;
    }
}
