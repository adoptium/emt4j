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

import org.eclipse.emt4j.common.rule.model.ConfRules;
import org.eclipse.emt4j.common.util.FileUtil;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A rule only supports one LTS to the next LTS, like from JDK 8 to JDK 11 and from JDK 11 to JDK 17.
 * Not support from JDK 8 to JDK 17.
 * But if the user wants to upgrade from JDK 8 to JDK 17, it needs to split into more than one rule, then
 * check with these rules, and merge into a single result finally.
 */
public class ConfRuleFacade {
    private final static int[] JDK_UPGRADE_ROADMAP;

    static {
        String[] roadMapStr = FileUtil.readPlainTextFromResource("/roadmap.cfg", false).get(0).split("->");
        JDK_UPGRADE_ROADMAP = new int[roadMapStr.length];
        for (int i = 0; i < roadMapStr.length; i++) {
            JDK_UPGRADE_ROADMAP[i] = Integer.parseInt(roadMapStr[i]);
        }
    }

    public static List<ConfRules> load(String[] features, String[] modes, int fromVersion, int toVersion) throws SAXException, IOException, URISyntaxException {
        int[][] roadmap = findWays(fromVersion, toVersion);
        if (roadmap == null) {
            throw new RuntimeException("Not a valid fromVersion: " + fromVersion + ",toVersion:" + toVersion + " pair!");
        }

        List<ConfRules> confRulesList = new ArrayList<>();
        for (int[] oneWay : roadmap) {
            for (String feature : features) {
                Optional<ConfRules> confRules = ConfRuleRepository.load(feature, oneWay[0], oneWay[1]);
                confRules.ifPresent((v) -> {
                    if (v.getRuleItems() != null) {
                        v.setRuleItems(v.getRuleItems().stream().
                                filter((r) -> intersect(r.getSupportModes(), modes))
                                .collect(Collectors.toList()));
                    }
                    confRulesList.add(v);
                });
            }
        }
        return confRulesList;
    }

    private static boolean intersect(List<String> supportModes, String[] modes) {
        for (String supportMode : supportModes) {
            for (String mode : modes) {
                if (supportMode.equals(mode)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static int[][] findWays(int fromVersion, int toVersion) {
        int fromIndex = -1;
        int toIndex = -1;
        for (int i = 0; i < JDK_UPGRADE_ROADMAP.length; i++) {
            if (JDK_UPGRADE_ROADMAP[i] == fromVersion) {
                fromIndex = i;
            } else if (JDK_UPGRADE_ROADMAP[i] == toVersion) {
                toIndex = i;
            }
        }
        if (fromIndex == -1 || toIndex == -1 || fromIndex == toIndex || toIndex < fromIndex) {
            return null;
        }
        int[][] ways = new int[toIndex - fromIndex][2];
        for (int i = fromIndex; i < toIndex; i++) {
            ways[i - fromIndex][0] = JDK_UPGRADE_ROADMAP[i];
            ways[i - fromIndex][1] = JDK_UPGRADE_ROADMAP[i + 1];
        }
        return ways;
    }

    public static String getFeatureI18nBase(String feature) {
        return feature + ".i18n.";
    }
}
