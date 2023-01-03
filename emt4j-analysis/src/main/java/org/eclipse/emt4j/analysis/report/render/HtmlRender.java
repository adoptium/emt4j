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

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.eclipse.emt4j.common.CheckResultContext;
import org.eclipse.emt4j.common.Dependency;
import org.eclipse.emt4j.common.Feature;
import org.eclipse.emt4j.common.ReportConfig;
import org.eclipse.emt4j.common.i18n.I18nResourceUnit;
import org.eclipse.emt4j.common.rule.ConfRuleFacade;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

public class HtmlRender extends VelocityTemplateRender {
    private final String DIR_OTHERS = "Others";

    public HtmlRender(ReportConfig config) {
        super(config);
    }

    @Override
    public void doRender(Map<String, List<CheckResultContext>> resultMap) throws IOException {
        VelocityEngine velocityEngine = new VelocityEngine();
        velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADERS, "classpath");
        velocityEngine.setProperty("resource.loader.classpath.class", ClasspathResourceLoader.class.getName());
        VelocityContext context = new VelocityContext();
        List<CategorizedResult> categorizedResultList = toCategorizedResult(resultMap);
        context.put("title", reportResourceAccessor.getString(ConfRuleFacade.getFeatureI18nBase("default"), "html.title"));
        context.put("data", categorizedResultList);
        List<CategoryContent> content = getContent(categorizedResultList);
        context.put("content", content);
        if (!useOldTemplate()) {
            int total = 0;
            for (CategoryContent dcc : content) {
                total += dcc.getTotal();
            }
            context.put("total",
                        String.format(
                                reportResourceAccessor.getString(ConfRuleFacade.getFeatureI18nBase("default"), "issue.foundInTotal"),
                                total,
                                total > 1 ? "s" : ""));
        }
        context.put("noIssue", reportResourceAccessor.getNoIssueResource(ConfRuleFacade.getFeatureI18nBase("default")));
        context.put("contentTitle", reportResourceAccessor.getString(ConfRuleFacade.getFeatureI18nBase("default"), "content.title"));
        context.put("detailTitle", reportResourceAccessor.getString(ConfRuleFacade.getFeatureI18nBase("default"), "detail.title"));
        context.put("backToContent", reportResourceAccessor.getString(ConfRuleFacade.getFeatureI18nBase("default"), "back.to.content"));

        File output = new File(config.getOutputFile()).getAbsoluteFile();
        if (!output.getParentFile().exists()) {
            Files.createDirectories(output.getParentFile().toPath().toAbsolutePath());
        }
        try (OutputStream out = new FileOutputStream(config.getOutputFile())) {
            Template template = velocityEngine.getTemplate(getTemplate());
            final Writer writer = new OutputStreamWriter(out, "UTF-8");
            template.merge(context, writer);
            writer.flush();
        }
    }

    private List<CategoryContent> getContent(List<CategorizedResult> categorizedResultList) {
        List<CategoryContent> dccList = new ArrayList<>();
        for (CategorizedResult cr : categorizedResultList) {
            CategoryContent dcc = new CategoryContent(cr.desc);
            dcc.setId(cr.getId());
            for (ResultDetail detail : cr.getResultDetailList()) {
                Content content = new Content(detail.getTitle(), detail.getAnchorId());
                content.setPriority(detail.priority);
                content.setTotal(detail.getContext().size());
                dcc.addContent(content);
            }
            if (!useOldTemplate()) {
                dcc.addDescription(
                        String.format(
                                reportResourceAccessor.getString(ConfRuleFacade.getFeatureI18nBase("default"),
                                                                 "issue.found"),
                                dcc.getTotal(),
                                dcc.getTotal() > 1 ? "s" : ""));
            }
            dccList.add(dcc);
        }
        return dccList;
    }

    private List<CategorizedResult> toCategorizedResult(Map<String, List<CheckResultContext>> resultMap) {
        List<CategorizedResult> list = new ArrayList<>();
        Collection<CheckResultContextHolder> holders = useOldTemplate() ? classifyByDir(resultMap) : classifyByName(resultMap);
        if (holders.isEmpty()) {
            return list;
        }
        holders.forEach(holder -> {
            Map<String, List<CheckResultContext>> tmp = new HashMap<>();
            tmp.put(Feature.DEFAULT.getId(), holder.contexts);
            CategorizedCheckResult categorizedCheckResult = categorize(tmp);
            if (!categorizedCheckResult.noResult()) {
                for (String feature : categorizedCheckResult.getFeatures()) {
                    int detailId = 0;
                    CategorizedResult cr = new CategorizedResult();
                    cr.desc = holder.name;
                    cr.anchorId = "cr-anchor" + holder.name.hashCode();
                    String i18nBase = ConfRuleFacade.getFeatureI18nBase(feature);
                    for (TreeMap<String, TreeMap<String, List<CheckResultContext>>> map : categorizedCheckResult.getResult().get(feature)) {
                        Iterator<Map.Entry<String, TreeMap<String, List<CheckResultContext>>>> iter = map.entrySet().iterator();
                        while (iter.hasNext()) {
                            Map.Entry<String, TreeMap<String, List<CheckResultContext>>> entry = iter.next();
                            Iterator<Map.Entry<String, List<CheckResultContext>>> subIter = entry.getValue().entrySet().iterator();
                            while (subIter.hasNext()) {
                                Map.Entry<String, List<CheckResultContext>> subEntry = subIter.next();
                                I18nResourceUnit resourceUnit = reportResourceAccessor.getResourceUnit(entry.getKey(), subEntry.getKey(), i18nBase);
                                resourceUnit.render(subEntry.getValue());
                                ResultDetail rd = new ResultDetail();
                                detailId++;
                                rd.detailId = detailId;
                                rd.mainResultCode = entry.getKey();
                                rd.subResultCode = subEntry.getKey();
                                rd.title = resourceUnit.getTitle();
                                rd.descriptionTitle = resourceUnit.getDescriptionTitle();
                                rd.description = resourceUnit.getDescription();
                                rd.solutionTitle = resourceUnit.getSolutionTitle();
                                rd.solution.addAll(resourceUnit.getSolutionSet());
                                rd.contextTitle = resourceUnit.getIssueContextTitle();
                                rd.context.addAll(resourceUnit.getIssueContextList());
                                rd.anchorId = "rd-anchor" + cr.anchorId + "-" + rd.detailId;
                                if (subEntry.getValue() != null && !subEntry.getValue().isEmpty()) {
                                    //for a given resultCode+subResultCode,the priority is same.
                                    rd.priority = subEntry.getValue().get(0).getReportCheckResult().getPriority();
                                }
                                cr.resultDetailList.add(rd);
                            }
                        }
                    }
                    list.add(cr);
                }
            }
        });
        list.sort(Comparator.comparing(CategorizedResult::getDesc));
        for (int i = 0; i < list.size(); i++) {
            list.get(i).setId(i + 1);
        }
        return list;
    }

    static class CheckResultContextHolder {
        String name;

        List<CheckResultContext> contexts = new ArrayList<>();

        public CheckResultContextHolder(String name) {
            this.name = name;
        }
    }

    private Collection<CheckResultContextHolder> classifyByDir(Map<String, List<CheckResultContext>> resultMap) {
        if (null == resultMap || resultMap.isEmpty()) {
            return Collections.emptyList();
        }
        Map<String, CheckResultContextHolder> dirCategoryMap = new HashMap<>();
        resultMap.forEach((f, v) -> v.forEach((c) -> {
            String categoryDesc = getCategoryDesc(c);
            dirCategoryMap.computeIfAbsent(categoryDesc, CheckResultContextHolder::new).contexts.add(c);
        }));
        return dirCategoryMap.values();
    }

    private Collection<CheckResultContextHolder> classifyByName(Map<String, List<CheckResultContext>> resultMap) {
        if (null == resultMap || resultMap.isEmpty()) {
            return Collections.emptyList();
        }
        Map<String, CheckResultContextHolder> categoryMap = new HashMap<>();
        CheckResultContextHolder dh = new CheckResultContextHolder(reportResourceAccessor.getString(ConfRuleFacade.getFeatureI18nBase("default"), "project.dependencies"));
        resultMap.forEach((f, v) -> v.forEach((c) -> {
            Dependency dependency = c.getDependency();
            if (c.getDependency().isDeps()) {
                dh.contexts.add(c);
            } else if (dependency.getName() != null){
                categoryMap.computeIfAbsent(dependency.getName(), CheckResultContextHolder::new).contexts.add(c);
            }
        }));
        if (dh.contexts.size() > 0) {
            ArrayList<CheckResultContextHolder> list = new ArrayList<>(categoryMap.values());
            list.add(dh);
            return list;
        }
        return categoryMap.values();
    }

    private String getCategoryDesc(CheckResultContext c) {
        try {
            String filePath = c.getDependency().getTargetFilePath();
            if (null == filePath || "".equals(filePath)) {
                return DIR_OTHERS;
            } else {
                return new File(filePath).getParent();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return DIR_OTHERS;
        }
    }

    @Override
    String getTemplate() {
        if (useOldTemplate()) {
            return "html-report-old.vm";
        }
        return "html-report.vm";
    }

    private static boolean useOldTemplate() {
        return Boolean.getBoolean("useOldTemplate");
    }

}
