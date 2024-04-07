/********************************************************************************
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
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
import org.eclipse.emt4j.common.Feature;
import org.eclipse.emt4j.common.ReportConfig;
import org.eclipse.emt4j.common.i18n.I18nResourceUnit;
import org.eclipse.emt4j.common.rule.ConfRuleFacade;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

import java.io.*;
import java.util.*;

public abstract class VelocityTemplateRender extends AbstractRender implements Render {
    public VelocityTemplateRender(ReportConfig config) {
        super(config);
    }

    @Override
    public void render(Map<Feature, List<CheckResultContext>> resultMap) throws IOException {
        VelocityEngine velocityEngine = new VelocityEngine();
        velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADERS, "classpath");
        velocityEngine.setProperty("resource.loader.classpath.class", ClasspathResourceLoader.class.getName());
        VelocityContext context = new VelocityContext();
        List<FeatureResult> featureResultList = toFeatureResult(resultMap);
        context.put("data", featureResultList);
        context.put("content", getContent(featureResultList));
        context.put("noIssue", reportResourceAccessor.getNoIssueResource(ConfRuleFacade.getFeatureI18nBase(Feature.DEFAULT)));
        context.put("contentTitle", reportResourceAccessor.getString(ConfRuleFacade.getFeatureI18nBase(Feature.DEFAULT), "content.title"));
        context.put("detailTitle", reportResourceAccessor.getString(ConfRuleFacade.getFeatureI18nBase(Feature.DEFAULT), "detail.title"));
        context.put("backToContent", reportResourceAccessor.getString(ConfRuleFacade.getFeatureI18nBase(Feature.DEFAULT), "back.to.content"));

        try (OutputStream out = new FileOutputStream(config.getOutputFile())) {
            Template template = velocityEngine.getTemplate(getTemplate());
            final Writer writer = new OutputStreamWriter(out, "UTF-8");
            template.merge(context, writer);
            writer.flush();
        }
    }

    private List<Content> getContent(List<FeatureResult> featureResultList) {
        List<Content> contents = new ArrayList<>();
        for (FeatureResult fr : featureResultList) {
            for (ResultDetail detail : fr.getResultDetailList()) {
                contents.add(new Content(detail.getTitle(), detail.getAnchorId()));
            }
        }
        return contents;
    }

    private List<FeatureResult> toFeatureResult(Map<Feature, List<CheckResultContext>> resultMap) {
        List<FeatureResult> list = new ArrayList<>();
        CategorizedCheckResult categorizedCheckResult = categorize(resultMap);
        if (categorizedCheckResult.noResult()) {
            return list;
        }

        int featureId = 0;
        for (Feature feature : categorizedCheckResult.getFeatures()) {
            featureId++;
            int detailId = 0;
            FeatureResult fr = new FeatureResult();
            fr.featureDesc = reportResourceAccessor.getCheckResultForFeature(ConfRuleFacade.getFeatureI18nBase(feature));
            fr.featureId = featureId;
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
                        rd.anchorId = fr.featureId + "-" + rd.detailId;
                        fr.resultDetailList.add(rd);
                    }
                }
            }
            list.add(fr);
        }
        return list;
    }

    abstract String getTemplate();
}
