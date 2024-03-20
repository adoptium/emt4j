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
package org.eclipse.emt4j.analysis.report.render;

import org.eclipse.emt4j.analysis.autofix.*;
import org.eclipse.emt4j.common.*;
import org.apache.maven.project.MavenProject;
import org.eclipse.emt4j.common.CheckResultContext;
import org.eclipse.emt4j.common.IssueContext;
import org.eclipse.emt4j.common.ReportConfig;
import org.eclipse.emt4j.common.SourceInformation;
import org.eclipse.emt4j.common.i18n.I18nResourceUnit;
import org.eclipse.emt4j.common.rule.ConfRuleFacade;

import java.io.IOException;
import java.util.*;

public class AutofixReportRenderer extends AbstractRender {

    public AutofixReportRenderer(ReportConfig config) {
        super(config);
    }

    @Override
    protected void render(Map<Feature, List<CheckResultContext>> resultMap) throws IOException {
        AutofixConfig autofixConfig = AutofixConfig.getInstance();
        AutofixReport autofixReport = AutofixReport.getInstance();
        assert autofixConfig.isAutofix();

        Map<Feature, List<CheckResultContext>> unfixedMap;
        unfixedMap = autofixReport.getUnFixedJavaAndDependency();

        ReportConfig reportConfig;
        String fixedReportFile = autofixConfig.getFixedReportFile();
        reportConfig = new ReportConfig(config);
        reportConfig.setOutputFile(fixedReportFile);
        new HtmlRendererWithAutofixReport(reportConfig, autofixReport.getOtherFixedInfo(), true).render(autofixReport.getFixedJavaAndDependency());

        reportConfig = new ReportConfig(config);
        reportConfig.setOutputFile(autofixConfig.getUnfixedReportFile());
        new HtmlRendererWithAutofixReport(reportConfig, autofixReport.getOtherUnfixedInfo(), false).render(unfixedMap);
    }

    @Override
    protected void logGeneratedFilePath() {
        AutofixConfig autofixConfig = AutofixConfig.getInstance();
        doLogGeneratedFilePath("Fixed report", autofixConfig.getFixedReportFile());
        doLogGeneratedFilePath("Unfixed report", autofixConfig.getUnfixedReportFile());
    }
}
