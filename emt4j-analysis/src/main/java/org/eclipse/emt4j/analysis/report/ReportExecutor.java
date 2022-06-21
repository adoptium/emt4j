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
package org.eclipse.emt4j.analysis.report;

import org.eclipse.emt4j.analysis.common.ReportInputProvider;
import org.eclipse.emt4j.analysis.common.model.ExternalToolParam;
import org.eclipse.emt4j.analysis.common.util.JdkUtil;
import org.eclipse.emt4j.analysis.common.util.Progress;
import org.eclipse.emt4j.analysis.report.external.Tool;
import org.eclipse.emt4j.analysis.report.external.impl.JdepTool;
import org.eclipse.emt4j.analysis.report.external.impl.JdeprscanTool;
import org.eclipse.emt4j.analysis.report.render.*;
import org.eclipse.emt4j.common.CheckResultContext;
import org.eclipse.emt4j.common.DependType;
import org.eclipse.emt4j.common.ReportConfig;
import org.eclipse.emt4j.common.fileformat.BodyRecord;
import org.eclipse.emt4j.common.fileformat.VariableHeader;
import org.eclipse.emt4j.common.util.ClassURL;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

/**
 * Do the real report work.
 * <ol>
 *     <li>Read immediate file that contains all problems</li>
 *     <li>Call external checking tools and merge into current result </li>
 *     <li>Render to final report file </li>
 * </ol>
 */
public class ReportExecutor {
    private final ReportConfig reportConfig;
    private Render render;

    public ReportExecutor(ReportConfig reportConfig) {
        this.reportConfig = reportConfig;
    }

    public void execute(ReportInputProvider reportInputProvider, Progress parentProgress) throws IOException, ClassNotFoundException, InterruptedException, URISyntaxException {
        ClassURL.registerUrlProtocolHandler();
        log("Reading checking result.");
        new Progress(parentProgress, "Read dependency records").printTitle();
        List<BodyRecord> recordList = reportInputProvider.getRecords();

        log("Prepare for invoking external tools.");
        new Progress(parentProgress, "Prepare for external tools").printTitle();
        ExternalToolParam etp = prepareExternalToolParam(recordList, reportInputProvider.getHeader());
        Progress runExternalProgress = new Progress(parentProgress, "Run external tools");
        runExternalProgress.printTitle();
        recordList.addAll(decorateWithExternalTools(etp, runExternalProgress));

        this.render = createRender();
        new Progress(parentProgress, "Write result to report file").printTitle();
        render.doRender(prepare(recordList));
    }

    private ExternalToolParam prepareExternalToolParam(List<BodyRecord> recordList, VariableHeader header) throws IOException, InterruptedException, URISyntaxException {
        ExternalToolParam etp = new ExternalToolParam();
        etp.setFromVersion(header.getFromVersion());
        etp.setToVersion(header.getToVersion());
        etp.setTargetJdkHome(getTargetJdkHome(header.getToVersion()));
        etp.setVmOption(header.getVmOption());
        etp.setFeatures(header.getFeatures());
        Iterator<BodyRecord> iter = recordList.iterator();
        while (iter.hasNext()) {
            BodyRecord br = iter.next();
            if (br.getCheckResult() == null) {
                if (br.getDependency().getDependType() == DependType.CODE_SOURCE) {
                    if (br.getDependency().getTarget().asLocation().getLocationExternalForm().startsWith("file:")) {
                        addIfNotNull(etp.getClassesOrJars(), new File(new URL(br.getDependency().getTarget().asLocation().getLocationExternalForm()).toURI()));
                    }
                } else if (br.getDependency().getDependType() == DependType.VM_OPTION) {
                    etp.setVmOption(br.getDependency().getTarget().asVMOption().getVmOption());
                }
                //this BodyRecord only used by external tools,so remove it before generate report
                iter.remove();
            }
        }
        return etp;
    }

    private <T> void addIfNotNull(Set<T> classesOrJars, T file) {
        if (file != null) {
            classesOrJars.add(file);
        }
    }

    private String getTargetJdkHome(int toVersion) throws IOException, InterruptedException {
        File targetJdkHome = JdkUtil.searchTargetJdk(reportConfig.getTargetJdkHome(), toVersion, reportConfig.isVerbose());
        if (targetJdkHome != null) {
            log("Find target JDK at " + targetJdkHome.getCanonicalPath());
            return targetJdkHome.getCanonicalPath();
        } else {
            log("Not found JDK with version " + toVersion + "!Some checks are ignored!");
            return null;
        }
    }

    private Map<String, List<CheckResultContext>> prepare(List<BodyRecord> recordList) {
        Map<String, List<CheckResultContext>> resultMap = new HashMap<>();
        for (BodyRecord record : recordList) {
            String feature = record.getFeature();
            CheckResultContext checkResultContext = new CheckResultContext(record.getCheckResult(), record.getDependency());
            if (resultMap.containsKey(feature)) {
                resultMap.get(feature).add(checkResultContext);
            } else {
                List<CheckResultContext> resultList = new ArrayList<>();
                resultList.add(checkResultContext);
                resultMap.put(feature, resultList);
            }
        }
        return resultMap;
    }

    private Render createRender() {
        String format = reportConfig.getOutputFormat();
        if ("json".equals(format)) {
            return new JsonRender(reportConfig);
        } else if ("txt".equals(format)) {
            return new TxtRender(reportConfig);
        } else if ("html".equals(format)) {
            return new HtmlRender(reportConfig);
        } else if ("api".equals(format)) {
            return new ApiRender(reportConfig);
        } else {
            throw new RuntimeException("Unsupported report format :" + format);
        }
    }

    private List<BodyRecord> decorateWithExternalTools(ExternalToolParam etp, Progress parentProgress) {
        List<BodyRecord> list = new ArrayList<>();
        for (Tool tool : getTools()) {
            try {
                Progress progress = new Progress(parentProgress, "Run tool " + tool.name());
                list.addAll(tool.analysis(etp, reportConfig, progress));
            } catch (Exception e) {
                //ignore exception. After all ,external tool is only icing on the cake
                e.printStackTrace();
            }
        }
        return list;
    }

    private Tool[] getTools() {
        return new Tool[]{new JdeprscanTool(), new JdepTool()};
    }

    public Render getRender() {
        return render;
    }

    private void log(String msg) {
        if (reportConfig.isVerbose()) {
            System.out.println(msg);
        }
    }
}
