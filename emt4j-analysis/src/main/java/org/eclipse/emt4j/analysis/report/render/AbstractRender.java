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
package org.eclipse.emt4j.analysis.report.render;

import org.eclipse.emt4j.common.CheckResultContext;
import org.eclipse.emt4j.common.MainResultDetail;
import org.eclipse.emt4j.common.ReportConfig;
import org.eclipse.emt4j.common.SubResultDetail;
import org.eclipse.emt4j.common.i18n.I18nResourceUnit;
import org.eclipse.emt4j.common.i18n.ReportResourceAccessor;
import org.eclipse.emt4j.common.rule.ConfRuleFacade;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class AbstractRender {

    protected ReportConfig config;
    protected ReportResourceAccessor reportResourceAccessor;

    public AbstractRender(ReportConfig config) {
        this.config = config;
        reportResourceAccessor = new ReportResourceAccessor(config.getLocale());
    }

    protected CategorizedCheckResult categorize(Map<String, List<CheckResultContext>> checkResultContextList) {
        CategorizedCheckResult categorizedCheckResult = new CategorizedCheckResult();
        for (String feature : checkResultContextList.keySet()) {
            Map<String, List<CheckResultContext>> priorityToCheckResult = checkResultContextList.get(feature).stream().collect(Collectors.groupingBy(c -> c.getReportCheckResult().getPriority()));
            Map<String, TreeMap<String, TreeMap<String, List<CheckResultContext>>>> priorityToTreeMap = new HashMap<>();
            priorityToCheckResult.forEach((k, v) -> {
                priorityToTreeMap.put(k, category(v, feature));
            });

            List<String> sortPriority = new ArrayList<>(priorityToCheckResult.keySet());
            sortPriority.sort(Comparator.naturalOrder());
            List<TreeMap<String, TreeMap<String, List<CheckResultContext>>>> sortTreeMap = new ArrayList<>();
            for (String priority : sortPriority) {
                sortTreeMap.add(priorityToTreeMap.get(priority));
            }
            categorizedCheckResult.getResult().put(feature, sortTreeMap);
            categorizedCheckResult.getFeatures().add(feature);
        }

        categorizedCheckResult.getFeatures().sort(Comparator.naturalOrder());
        return categorizedCheckResult;
    }

    private TreeMap<String, TreeMap<String, List<CheckResultContext>>> category(List<CheckResultContext> checkResultContextList, String feature) {
        TreeMap<String, TreeMap<String, List<CheckResultContext>>> resultMap = new TreeMap<>();
        for (CheckResultContext crc : checkResultContextList) {
            String main = crc.getReportCheckResult().getResultCode();
            String sub = crc.getReportCheckResult().getSubResultCode();
            TreeMap<String, List<CheckResultContext>> subTree = resultMap.get(main);
            if (subTree == null) {
                subTree = new TreeMap<>();
                resultMap.put(crc.getReportCheckResult().getResultCode(), subTree);
            }

            String effective = "";
            //Ignore sub result code if no specific corresponding resource bundle
            if (StringUtils.isNotEmpty(sub) &&
                    reportResourceAccessor.containResourceBundle(main, sub, ConfRuleFacade.getFeatureI18nBase(feature))) {
                effective = sub;
            }

            List<CheckResultContext> value = subTree.get(effective);
            if (value == null) {
                value = new ArrayList<>();
                subTree.put(effective, value);
            }
            value.add(crc);
        }
        return resultMap;
    }

    protected void convert(TreeMap<String, TreeMap<String, List<CheckResultContext>>> resultMap, List<MainResultDetail> resultDetailList, String i18nBase) {
        Iterator<Map.Entry<String, TreeMap<String, List<CheckResultContext>>>> iter = resultMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, TreeMap<String, List<CheckResultContext>>> entry = iter.next();
            MainResultDetail mainResultDetail = createIfNotExist(resultDetailList,
                    (m) -> m.getMainResultCode().equals(entry.getKey()),
                    () -> new MainResultDetail());
            mainResultDetail.setMainResultCode(entry.getKey());
            Iterator<Map.Entry<String, List<CheckResultContext>>> subIter = entry.getValue().entrySet().iterator();
            while (subIter.hasNext()) {
                Map.Entry<String, List<CheckResultContext>> subEntry = subIter.next();
                I18nResourceUnit resourceUnit = reportResourceAccessor.getResourceUnit(entry.getKey(), subEntry.getKey(), i18nBase);
                mainResultDetail.getSubResultDetailList().add(buildSubResultCode(entry.getKey(), subEntry.getKey(), resourceUnit, subEntry.getValue(), i18nBase));
            }
        }
    }

    private SubResultDetail buildSubResultCode(String mainResultCode, String subResultCode, I18nResourceUnit resourceUnit, List<CheckResultContext> checkResultContextList, String i18nBase) {
        resourceUnit.render(checkResultContextList);
        SubResultDetail subResultDetail = new SubResultDetail();
        subResultDetail.setSubResultCode(subResultCode);
        subResultDetail.setBriefDesc(resourceUnit.getTitle());
        subResultDetail.getMoreDetailDesc().add(resourceUnit.getDescription());
        subResultDetail.getHowToFix().addAll(resourceUnit.getSolutionSet());
        subResultDetail.setIssueContextList(resourceUnit.getIssueContextList());
        return subResultDetail;
    }

    private <T> T createIfNotExist(List<T> list, Predicate<T> exist, Supplier<T> creator) {
        for (T t : list) {
            if (exist.test(t)) {
                return t;
            }
        }
        T t = creator.get();
        list.add(t);
        return t;
    }
}
