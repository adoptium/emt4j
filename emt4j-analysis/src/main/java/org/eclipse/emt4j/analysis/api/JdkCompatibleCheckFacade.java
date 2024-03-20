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
package org.eclipse.emt4j.analysis.api;

import org.eclipse.emt4j.analysis.AnalysisExecutor;
import org.eclipse.emt4j.analysis.common.model.CheckTargetTypeEnum;
import org.eclipse.emt4j.analysis.common.model.JdkCheckCompatibleResult;
import org.eclipse.emt4j.analysis.common.model.ToCheckTarget;
import org.eclipse.emt4j.analysis.common.util.Progress;
import org.eclipse.emt4j.analysis.out.MemoryHolderOutputConsumer;
import org.eclipse.emt4j.analysis.report.ReportExecutor;
import org.eclipse.emt4j.analysis.report.render.ApiRender;
import org.eclipse.emt4j.analysis.common.model.JdkCheckCompatibleRequest;
import org.eclipse.emt4j.analysis.source.DependencySource;
import org.eclipse.emt4j.analysis.source.SingleJarSource;
import org.eclipse.emt4j.common.*;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;

/**
 * This facade API provides a memory-based implementation.
 */
public final class JdkCompatibleCheckFacade {
    private final static int[][] VALID_FROM_TO_VERSION_PAIR = new int[][]{
            {8, 11},
            {11, 17},
            {8, 17},
            {8, 21},
            {11, 21},
            {17, 21}};

    public static JdkCheckCompatibleResult check(JdkCheckCompatibleRequest request) throws IOException, InterruptedException, ClassNotFoundException, URISyntaxException {
        checkParam(request);
        CheckConfig checkConfig = getCheckConfig(request);
        MemoryHolderOutputConsumer outputConsumer = new MemoryHolderOutputConsumer();
        AnalysisExecutor analysisExecutor = new AnalysisExecutor(checkConfig);
        analysisExecutor.setAnalysisOutputConsumer(outputConsumer);
        for (ToCheckTarget checkTarget : request.getToCheckTargetList()) {
            analysisExecutor.add(convert(request.getIdentifier(), checkTarget));
        }
        Progress progress = new Progress(0, 1, "JDK Compatible API Check");
        analysisExecutor.execute(Collections.singletonList(Feature.DEFAULT), progress);

        ReportConfig reportConfig = new ReportConfig();
        reportConfig.setOutputFormat("api");
        reportConfig.setLocale(request.getReportLocale());
        reportConfig.setTargetJdkHome(request.getTargetJdkHome());
        reportConfig.setVerbose(request.isVerbose());
        reportConfig.setExternalToolRoot(request.getExternalToolHome());
        ReportExecutor reportExecutor = new ReportExecutor(reportConfig);
        reportExecutor.execute(outputConsumer.getInputProvider(), progress, request.isDisableExternalTool());

        JdkCheckCompatibleResult result = new JdkCheckCompatibleResult();
        result.setResultDetailList(((ApiRender) reportExecutor.getRender()).getResultDetailList());
        return result;
    }

    private static DependencySource convert(String identifier, ToCheckTarget checkTarget) {
        if (checkTarget.getTargetType() == CheckTargetTypeEnum.JAR
                || checkTarget.getTargetType() == CheckTargetTypeEnum.ClASS) {
            File f = new File(checkTarget.getTargetIdentifier());
            if (f.isFile() && f.exists()) {
                SingleJarSource source = new SingleJarSource(f);
                SourceInformation sourceInformation = new SourceInformation();
                sourceInformation.setFullIdentifier(identifier);
                source.setInformation(sourceInformation);
                return source;
            } else {
                throw new JdkMigrationException("Jar file " + checkTarget.getTargetIdentifier() + " not valid!");
            }
        } else {
            throw new JdkMigrationException("Unsupported targetType:" + checkTarget.getTargetType());
        }
    }

    static CheckConfig getCheckConfig(JdkCheckCompatibleRequest request) {
        CheckConfig checkConfig = new CheckConfig();
        checkConfig.setCheckMode(Feature.DEFAULT);
        checkConfig.setToVersion(request.getToVersion());
        checkConfig.setFromVersion(request.getFromVersion());
        checkConfig.setPriority(request.getPriority());
        checkConfig.setVerbose(request.isVerbose());
        return checkConfig;
    }

    private static void checkParam(JdkCheckCompatibleRequest request) {
        if (request == null) {
            throw new JdkMigrationException("Request cannot empty");
        }
        if (!isSupportVersion(request.getFromVersion(), request.getToVersion())) {
            throw new JdkMigrationException("FromVersion and ToVersion not valid!");
        }
        if (request.getReportLocale() == null) {
            throw new JdkMigrationException("ReportLocale cannot null");
        }
        if (!isSupportLocale(request)) {
            throw new JdkMigrationException("Language only support en or zh");
        }
        if (null == request.getToCheckTargetList() || request.getToCheckTargetList().isEmpty()) {
            throw new JdkMigrationException("No need to check?");
        }
        for (int i = 0; i < request.getToCheckTargetList().size(); i++) {
            ToCheckTarget checkTarget = request.getToCheckTargetList().get(i);
            if (checkTarget.getTargetType() == null || StringUtils.isEmpty(checkTarget.getTargetIdentifier())) {
                throw new JdkMigrationException("The " + (i + 1) + "th element of toCheckTarget not valid!");
            }
        }
    }

    private static boolean isSupportLocale(JdkCheckCompatibleRequest request) {
        return (request.getReportLocale().getLanguage().equalsIgnoreCase("zh")
                || request.getReportLocale().getLanguage().equalsIgnoreCase("en"));
    }

    private static boolean isSupportVersion(int fromVersion, int toVersion) {
        for (int[] fromTo : VALID_FROM_TO_VERSION_PAIR) {
            if (fromTo[0] == fromVersion && fromTo[1] == toVersion) {
                return true;
            }
        }
        return false;
    }
}
