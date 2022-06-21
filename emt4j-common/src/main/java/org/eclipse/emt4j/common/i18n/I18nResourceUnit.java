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
package org.eclipse.emt4j.common.i18n;

import org.eclipse.emt4j.common.util.CheckResultGroupUtil;
import org.eclipse.emt4j.common.*;
import org.mvel2.templates.TemplateRuntime;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Check result is an internal form, it needs to merge with miscellaneous I18 resources, then
 * generate a readable form.
 * With the helper <code>ReportResourceAccessor</code>, it translate check result to these parts:
 * <ol>
 *     <li>Title</li>
 *     <li>Description</li>
 *     <li>Solution</li>
 *     <li>Context</li>
 * </ol>
 */
public class I18nResourceUnit {

    /**
     * When two call stack trace is nearly the same, it tries to merge
     * the call stack into a single to save spaces.
     * Use this flag to show the difference when merging into a single one.
     */
    protected static final String SIGN = "+++ ";

    /**
     * access to i18 resource bundle
     */
    private final ReportResourceAccessor reportResourceAccessor;
    private final String i18nBase;
    private String title;

    private String description;

    private String descriptionTitle;

    private String solution;

    private String solutionTitle;

    private String issueContextTitle;

    private String resultCode;

    private String subResultCode;

    private Set<String> solutionSet = new HashSet<>();

    private List<IssueContext> issueContextList = new ArrayList<>();

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void render(List<CheckResultContext> checkResultContextList) {
        renderSolution(checkResultContextList);
        renderIssueContext(checkResultContextList);
    }

    private void renderIssueContext(List<CheckResultContext> checkResultContextList) {
        for (CheckResultGroup checkResultGroup : CheckResultGroupUtil.group(checkResultContextList)) {
            Set<String> issueContextSet = getIssueContext(checkResultGroup, i18nBase);
            if (!issueContextSet.isEmpty()) {
                IssueContext issueContext = new IssueContext();
                issueContextList.add(issueContext);
                issueContext.getContextDesc().addAll(issueContextSet);
                //try to merge similar call stack into a single one if there only one difference.
                if (checkResultGroup.getStackTrace() != null && !checkResultGroup.getStackTrace().isEmpty()) {
                    for (int i = 0; i < checkResultGroup.getStackTrace().size(); i++) {
                        boolean moreThenMoreFrame = false;
                        for (CallFrame frame : checkResultGroup.getDiffStackTraceFrame()) {
                            if (frame.getPos() == i) {
                                issueContext.getStackTrace().add(SIGN + frame.getFrameContent());
                                moreThenMoreFrame = true;
                            }
                        }
                        issueContext.getStackTrace().add(moreThenMoreFrame ? SIGN + checkResultGroup.getStackTrace().get(i) : checkResultGroup.getStackTrace().get(i));
                    }
                }
            }
        }
    }

    public Optional<String> getIssueContext(CheckResultContext checkResult, String i18nBase) {
        Dependency dependency = checkResult.getDependency();
        ResourceBundle common = reportResourceAccessor.getCommonResourceBundle(i18nBase);
        if (dependency.getLocationExternalForm() != null) {
            return buildIssueContext(String.format(common.getString("issue.context.location.target"),
                    dependency.getLocationExternalForm(), dependency.getTarget().desc()));
        } else {
            return buildIssueContext(String.format(common.getString("issue.context.target"),
                    dependency.getTarget().desc()));
        }
    }

    private void renderSolution(List<CheckResultContext> checkResultContextList) {
        // 2. how to fix
        for (CheckResultContext checkResult : checkResultContextList) {
            //if context is not null,mean it's a dynamic solution,so need mvel2 interpret it.
            if (checkResult.getReportCheckResult().getContext() != null) {
                String result = ((String) TemplateRuntime.eval(solution, checkResult.getReportCheckResult().getContext())).trim();
                for (String s : result.split("\\\\newline")) {
                    if (null == s || "".equals(s.trim())) {
                        continue;
                    }
                    solutionSet.add(s.trim());
                }
            } else {
                solutionSet.add(solution);
            }
        }
    }

    private Optional<String> buildIssueContext(String contextStr) {
        return contextStr == null || "".equals(contextStr) ? Optional.empty() : Optional.of(contextStr);
    }

    private Set<String> getIssueContext(CheckResultGroup checkResultGroup, String i18nBase) {
        return checkResultGroup.getCheckResultContextList().stream().map((c) -> getIssueContext(c, i18nBase)).filter((c -> c.isPresent()))
                .map((c) -> c.get()).collect(Collectors.toSet());
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescriptionTitle() {
        return descriptionTitle;
    }

    public void setDescriptionTitle(String descriptionTitle) {
        this.descriptionTitle = descriptionTitle;
    }

    public String getSolution() {
        return solution;
    }

    public void setSolution(String solution) {
        this.solution = solution;
    }

    public String getSolutionTitle() {
        return solutionTitle;
    }

    public void setSolutionTitle(String solutionTitle) {
        this.solutionTitle = solutionTitle;
    }

    public String getIssueContextTitle() {
        return issueContextTitle;
    }

    public void setIssueContextTitle(String issueContextTitle) {
        this.issueContextTitle = issueContextTitle;
    }

    public I18nResourceUnit(String resultCode, String subResultCode, ReportResourceAccessor reportResourceAccessor, String i18nBase) {
        this.resultCode = resultCode;
        this.subResultCode = subResultCode;
        this.reportResourceAccessor = reportResourceAccessor;
        this.i18nBase = i18nBase;
    }

    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public String getSubResultCode() {
        return subResultCode;
    }

    public void setSubResultCode(String subResultCode) {
        this.subResultCode = subResultCode;
    }

    public Set<String> getSolutionSet() {
        return solutionSet;
    }

    public void setSolutionSet(Set<String> solutionSet) {
        this.solutionSet = solutionSet;
    }

    public List<IssueContext> getIssueContextList() {
        return issueContextList;
    }

    public void setIssueContextList(List<IssueContext> issueContextList) {
        this.issueContextList = issueContextList;
    }
}
