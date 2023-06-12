/********************************************************************************
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
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
import org.eclipse.emt4j.common.Feature;
import org.eclipse.emt4j.common.ReportConfig;
import org.eclipse.emt4j.common.SourceInformation;
import org.eclipse.emt4j.common.i18n.I18nResourceUnit;
import org.eclipse.emt4j.common.rule.ConfRuleFacade;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

public class HtmlRender extends VelocityTemplateRender {
    private final String DIR_OTHERS = "Others";

    public HtmlRender(ReportConfig config) {
        super(config);
    }

    @Override
    public void doRender(Map<Feature, List<CheckResultContext>> resultMap) throws IOException {
        VelocityEngine velocityEngine = new VelocityEngine();
        velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADERS, "classpath");
        velocityEngine.setProperty("resource.loader.classpath.class", ClasspathResourceLoader.class.getName());
        VelocityContext context = new VelocityContext();
        List<CategorizedResult> categorizedResultList = toCategorizedResult(resultMap);
        context.put("title", reportResourceAccessor.getString(ConfRuleFacade.getFeatureI18nBase(Feature.DEFAULT), "html.title"));
        context.put("data", categorizedResultList);
        List<CategorizedContent> ccs = getCategorizedContents(categorizedResultList);
        context.put("content", ccs);
        context.put("noIssue", reportResourceAccessor.getNoIssueResource(ConfRuleFacade.getFeatureI18nBase(Feature.DEFAULT)));
        context.put("contentTitle", reportResourceAccessor.getString(ConfRuleFacade.getFeatureI18nBase(Feature.DEFAULT), "content.title"));
        context.put("detailTitle", reportResourceAccessor.getString(ConfRuleFacade.getFeatureI18nBase(Feature.DEFAULT), "detail.title"));
        context.put("backToContent", reportResourceAccessor.getString(ConfRuleFacade.getFeatureI18nBase(Feature.DEFAULT), "back.to.content"));
        if (!useOldTemplate()) {
            int total = 0;
            for (CategorizedContent cc : ccs) {
                total += cc.getTotal();
            }
            context.put("total",
                    String.format(
                            reportResourceAccessor.getString(ConfRuleFacade.getFeatureI18nBase(Feature.DEFAULT), "issue.foundInTotal"),
                            total,
                            total > 1 ? "s" : ""));

            context.put("priority", reportResourceAccessor.getString(ConfRuleFacade.getFeatureI18nBase(Feature.DEFAULT), "issue.priority"));
            context.put("count", reportResourceAccessor.getString(ConfRuleFacade.getFeatureI18nBase(Feature.DEFAULT), "issue.count"));
        }

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

    private List<CategorizedContent> getCategorizedContents(List<CategorizedResult> categorizedResultList) {
        List<CategorizedContent> ccs = new ArrayList<>();
        for (CategorizedResult cr : categorizedResultList) {
            CategorizedContent cc = new CategorizedContent(cr.desc);
            cc.setId(cr.getId());
            for (ResultDetail detail : cr.getResultDetailList()) {
                Content content = new Content(detail.getTitle(), detail.getAnchorId());
                content.setPriority(detail.priority);
                content.setTotal(detail.getContext().size());
                cc.addSubContent(content);
            }
            if (!useOldTemplate()) {
                if (cr.getExtras() != null) {
                    for (String extra : cr.getExtras()) {
                        cc.addDescription(extra);
                    }
                }
                cc.addDescription(
                        String.format(
                                reportResourceAccessor.getString(ConfRuleFacade.getFeatureI18nBase(Feature.DEFAULT),
                                        "issue.found"),
                                cc.getTotal(),
                                cc.getTotal() > 1 ? "s" : ""));
            }
            ccs.add(cc);
        }
        return ccs;
    }

    private List<CategorizedResult> toCategorizedResult(Map<Feature, List<CheckResultContext>> resultMap) {
        List<CategorizedResult> list = new ArrayList<>();
        Collection<CheckResultContextHolder> holders = useOldTemplate() ? classifyByDir(resultMap) : classifyByIdentifier(resultMap);
        if (holders.isEmpty()) {
            return list;
        }
        holders.forEach(holder -> {
            Map<Feature, List<CheckResultContext>> tmp = new HashMap<>();
            tmp.put(holder.feature, holder.contexts);
            CategorizedCheckResult categorizedCheckResult = categorize(tmp);
            if (!categorizedCheckResult.noResult()) {
                for (Feature feature : categorizedCheckResult.getFeatures()) {
                    int detailId = 0;
                    CategorizedResult cr = new CategorizedResult();
                    cr.desc = holder.sourceInformation.getIdentifier();
                    cr.setExtras(holder.sourceInformation.getExtras());
                    cr.anchorId = "cr-anchor" + holder.sourceInformation.getIdentifier().hashCode();
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
        if (useOldTemplate()) {
            list.sort(Comparator.comparing(CategorizedResult::getDesc));
        }
        for (int i = 0; i < list.size(); i++) {
            list.get(i).setId(i + 1);
        }
        return list;
    }

    static class CheckResultContextHolder {

        SourceInformation sourceInformation;

        Feature feature;

        List<CheckResultContext> contexts = new ArrayList<>();

        public CheckResultContextHolder(SourceInformation sourceInformation, Feature feature) {
            this.sourceInformation = sourceInformation;
            this.feature = feature;
        }

        public CheckResultContextHolder(String desc, Feature feature) {
            sourceInformation = new SourceInformation();
            sourceInformation.setIdentifier(desc);
            this.feature = feature;
        }
    }

    private Collection<CheckResultContextHolder> classifyByDir(Map<Feature, List<CheckResultContext>> resultMap) {
        if (null == resultMap || resultMap.isEmpty()) {
            return Collections.emptyList();
        }
        Map<String, Map<Feature, CheckResultContextHolder>> categorizedMap = new HashMap<>();
        resultMap.forEach((f, v) -> v.forEach((c) -> {
            String categoryDesc = getCategoryDesc(c);
            categorizedMap
                    .computeIfAbsent(categoryDesc, _k -> new HashMap<>())
                    .computeIfAbsent(f, _f -> new CheckResultContextHolder(categoryDesc, f))
                    .contexts.add(c);
        }));
        return categorizedMap.values().stream().flatMap(m -> m.values().stream()).collect(Collectors.toList());
    }

    private Collection<CheckResultContextHolder> classifyByIdentifier(Map<Feature, List<CheckResultContext>> resultMap) {
        if (null == resultMap || resultMap.isEmpty()) {
            return Collections.emptyList();
        }
        Map<SourceInformation, Map<Feature, CheckResultContextHolder>> categorizedMap = new HashMap<>();
        SourceInformation info4dep = new SourceInformation();
        info4dep.setDependency(true);
        info4dep.setIdentifier(reportResourceAccessor.getString(ConfRuleFacade.getFeatureI18nBase(Feature.DEFAULT), "project.dependencies"));
        CheckResultContextHolder dh = new CheckResultContextHolder(info4dep, Feature.DEFAULT);
        Set<SourceInformation> depSet = new HashSet<>();
        resultMap.forEach((f, v) -> {
            v.forEach((c) -> {
                SourceInformation sourceInformation;
                if (c.getDependency().getSourceInformation() != null) {
                    sourceInformation = c.getDependency().getSourceInformation();
                } else {
                    sourceInformation = new SourceInformation();
                    sourceInformation.setIdentifier(getCategoryDesc(c));
                }
                if (sourceInformation.isDependency()) {
                    dh.contexts.add(c);
                    depSet.add(sourceInformation);
                } else if (sourceInformation.getIdentifier() != null) {
                    categorizedMap
                            .computeIfAbsent(sourceInformation, _k -> new HashMap<>())
                            .computeIfAbsent(f, _s -> new CheckResultContextHolder(sourceInformation, f))
                            .contexts.add(c);
                }
            });
        });
        Collection<CheckResultContextHolder> c = categorizedMap.values().stream()
                .flatMap(m -> m.values().stream()).collect(Collectors.toList());
        if (dh.contexts.size() > 0) {
            ArrayList<CheckResultContextHolder> list = new ArrayList<>(c);
            dh.sourceInformation.setExtras(new String[]{reportResourceAccessor.getString(ConfRuleFacade.getFeatureI18nBase(Feature.DEFAULT), "project.dependencyCount") + ": " + depSet.size()});
            list.add(dh);
            return list;
        }
        return c;
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
