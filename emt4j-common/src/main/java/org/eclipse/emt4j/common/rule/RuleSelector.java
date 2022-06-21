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

import org.eclipse.emt4j.common.RuleImpl;
import org.eclipse.emt4j.common.util.CollectionUtil;

import java.lang.annotation.Annotation;
import java.util.*;

/**
 * If a give rule type there more than one
 * implementation,<code>RuleSelector</code> select the highest priority implementation in current classpath.
 */
public class RuleSelector {
    public static Map<String, Class> select(String[] classList) throws ClassNotFoundException {
        Set<Class> classesSet = new HashSet<>();
        for (String className : classList) {
            classesSet.add(Class.forName(className));
        }

        //if there more than one with same type,select the highest priority
        Map<String, Map<Integer, Class>> typePriorityMap = new HashMap<>();
        for (Class c : classesSet) {
            Annotation annotation = c.getAnnotation(RuleImpl.class);
            if (annotation == null) {
                continue;
            }

            if (annotation instanceof RuleImpl) {
                RuleImpl ruleImplAnnotation = (RuleImpl) annotation;
                if (typePriorityMap.containsKey(ruleImplAnnotation.type())) {
                    typePriorityMap.get(ruleImplAnnotation.type()).put(ruleImplAnnotation.priority(), c);
                } else {
                    typePriorityMap.put(ruleImplAnnotation.type(), CollectionUtil.singleMap(ruleImplAnnotation.priority(), c));
                }
            }
        }

        Map<String, Class> highestPriority = new HashMap<>();
        typePriorityMap.forEach((k, v) -> highestPriority.put(k, v.get(Collections.min(v.keySet()))));
        return highestPriority;
    }
}
