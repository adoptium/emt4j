/********************************************************************************
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
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
package org.eclipse.emt4j.analysis.autofix;

import org.apache.velocity.VelocityContext;
import org.eclipse.emt4j.analysis.autofix.AutofixReport.FixedInfo;
import org.eclipse.emt4j.analysis.report.render.*;
import org.eclipse.emt4j.common.CheckResultContext;
import org.eclipse.emt4j.common.Feature;
import org.eclipse.emt4j.common.IssueContext;
import org.eclipse.emt4j.common.ReportConfig;
import org.eclipse.emt4j.common.i18n.I18nResourceUnit;
import org.eclipse.emt4j.common.rule.ConfRuleFacade;

import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class HtmlRendererWithAutofixReport extends HtmlRender {
    private final List<FixedInfo> fixedInfos;
    private final boolean checkAreFixed;

    public HtmlRendererWithAutofixReport(ReportConfig config, Collection<FixedInfo> fixedInfos, boolean checkAreFixed) {
        super(config);
        this.fixedInfos = new ArrayList<>(fixedInfos);
        this.checkAreFixed = checkAreFixed;
    }

    @Override
    protected VelocityContext prepareVelocityContext(Map<Feature, List<CheckResultContext>> resultMap) {
        VelocityContext context = super.prepareVelocityContext(resultMap);
        injectFixInfoToContext(context);
        return context;
    }

    @Override
    protected String getIssueFoundKey() {
        return checkAreFixed ? "autofix.fixed" : "autofix.unfixed";
    }

    @Override
    protected String getIssueFoundTotalKey() {
        return checkAreFixed ? "autofix.fixedInTotal" : "autofix.unfixedInTotal";
    }

    private void injectFixInfoToContext(VelocityContext context) {
        if (fixedInfos == null || fixedInfos.isEmpty()) {
            return;
        }
        List<CategorizedContent> contents = (List<CategorizedContent>) context.get("content");
        CategorizedContent categorizedContent = new CategorizedContent(reportResourceAccessor.getString(ConfRuleFacade.getFeatureI18nBase(Feature.DEFAULT), "other"));
        String parentAnchorID = "fix-anchor" + categorizedContent.getTitle().hashCode();
        categorizedContent.setId(contents.size() + 1);
        int total = AutofixReport.getOtherCount(fixedInfos);
        categorizedContent.addTotal(total);
        categorizedContent.addDescription(String.format(
                reportResourceAccessor.getString(ConfRuleFacade.getFeatureI18nBase(Feature.DEFAULT),
                        getIssueFoundKey()), total, total > 1 ? "s" : ""));
        List<Content> subContents = new ArrayList<>();
        for (int i = 0; i < fixedInfos.size(); i++) {
            FixedInfo fixedInfo = fixedInfos.get(i);
            String titleFormat = reportResourceAccessor.getAutofixDesc(fixedInfo.getFixType());
            String title = String.format(titleFormat, fixedInfo.getI18nParams());
            Content content = new Content(title, parentAnchorID + "-" + (i + 1));
            content.setPriority("p1");
            content.setTotal(fixedInfo.getFixCount());
            subContents.add(content);
        }
        categorizedContent.setSubContents(subContents);
        contents.add(categorizedContent);

        CategorizedResult categorizedResult = new CategorizedResult();
        categorizedResult.setProblemCount(categorizedContent.getTotal());
        categorizedResult.setDesc(categorizedContent.getTitle());
        categorizedResult.setAnchorId(parentAnchorID);
        categorizedResult.setId(categorizedContent.getId());

        List<ResultDetail> resultDetails = new ArrayList<>();
        for (int i = 0; i < fixedInfos.size(); i++) {
            FixedInfo fixedInfo = fixedInfos.get(i);
            Content content = categorizedContent.getSubContents().get(i);
            ResultDetail detail = new ResultDetail();
            detail.setDetailId(i + 1);
            detail.setMainResultCode(fixedInfo.getFixType());
            detail.setSubResultCode("");
            detail.setTitle(content.getTitle());
            if (fixedInfo.getFiles() != null) {
                detail.setContextTitle(reportResourceAccessor.getString(ConfRuleFacade.getFeatureI18nBase(Feature.DEFAULT), "issue.context.title"));
                detail.setContext(fixedInfo.getFiles().stream().map(file -> {
                    IssueContext issueContext = new IssueContext();
                    issueContext.setContextDesc(Collections.singletonList(file));
                    return issueContext;
                }).collect(Collectors.toSet()));
            }
            resultDetails.add(detail);
        }
        categorizedResult.setResultDetailList(resultDetails);
        ((List<CategorizedResult>) context.get("data")).add(categorizedResult);

        total = contents.stream().mapToInt(CategorizedContent::getTotal).sum();
        context.put("total",
                String.format(
                        reportResourceAccessor.getString(ConfRuleFacade.getFeatureI18nBase(Feature.DEFAULT), getIssueFoundTotalKey()),
                        total,
                        total > 1 ? "s" : ""));
    }
}
