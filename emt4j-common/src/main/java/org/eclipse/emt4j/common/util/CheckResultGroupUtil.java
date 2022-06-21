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
package org.eclipse.emt4j.common.util;

import org.eclipse.emt4j.common.CheckResultContext;
import org.eclipse.emt4j.common.CheckResultGroup;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Use merge many stack traces into a single stack trace if there is only a little difference.
 */
public class CheckResultGroupUtil {

    public static List<CheckResultGroup> group(List<CheckResultContext> checkResultContextList) {

        LinkedList<CheckResultContext> workset = new LinkedList<>(checkResultContextList);
        List<CheckResultGroup> groups = new ArrayList<>();
        while (!workset.isEmpty()) {
            CheckResultContext first = workset.removeFirst();
            List<String> baseStackTrace = stackTraceToArray(first);
            CheckResultGroup group = CheckResultGroup.createBase(first, baseStackTrace);
            groups.add(group);
            if (!baseStackTrace.isEmpty() && !workset.isEmpty()) {
                Iterator<CheckResultContext> iter = workset.iterator();
                while (iter.hasNext()) {
                    CheckResultContext toCompare = iter.next();
                    List<String> nowStackTrace = stackTraceToArray(toCompare);
                    //when no stack trace,there no need to merge into group
                    if (nowStackTrace.isEmpty()) {
                        groups.add(CheckResultGroup.createBase(toCompare, stackTraceToArray(toCompare)));
                        iter.remove();
                    } else {
                        if (baseStackTrace.size() == nowStackTrace.size()) {
                            findDiff(baseStackTrace, nowStackTrace, (diffPos) -> {
                                iter.remove();
                                if (diffPos != -1) {
                                    group.appendStackTraceDifference(toCompare, diffPos, nowStackTrace.get(diffPos));
                                }
                            });
                        }
                    }
                }
            }
        }

        return groups;
    }

    private static void findDiff(List<String> baseStackTrace, List<String> nowStackTrace, Consumer<Integer> diffConsumer) {
        int lastDiffPos = -1;
        int diffNum = 0;
        for (int i = 0; i < baseStackTrace.size(); i++) {
            if (!baseStackTrace.get(i).equals(nowStackTrace.get(i))) {
                lastDiffPos = i;
                diffNum++;
                //we only accept once difference.so if there more than 1.
                // we no need compare more.
                if (diffNum > 1) {
                    break;
                }
            }
        }

        if (diffNum == 0 || diffNum == 1) {
            //remove from workset,and merge to exist group
            diffConsumer.accept(lastDiffPos);
        }
    }


    private static List<String> stackTraceToArray(CheckResultContext checkResultContext) {
        if (checkResultContext.getDependency().getStacktrace() != null) {
            return Arrays.stream(checkResultContext.getDependency().getStacktrace()).map((s) -> s.toString()).collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }
}
