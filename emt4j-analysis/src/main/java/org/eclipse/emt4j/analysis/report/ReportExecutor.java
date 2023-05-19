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
import org.eclipse.emt4j.analysis.report.external.ExternalToolFailException;
import org.eclipse.emt4j.analysis.report.external.ModifyReportTool;
import org.eclipse.emt4j.analysis.report.external.Tool;
import org.eclipse.emt4j.analysis.report.render.ApiRender;
import org.eclipse.emt4j.analysis.report.render.HtmlRender;
import org.eclipse.emt4j.analysis.report.render.JsonRender;
import org.eclipse.emt4j.analysis.report.render.Render;
import org.eclipse.emt4j.analysis.report.render.TxtRender;
import org.eclipse.emt4j.common.CheckResultContext;
import org.eclipse.emt4j.common.DependType;
import org.eclipse.emt4j.common.ReportConfig;
import org.eclipse.emt4j.common.fileformat.BodyRecord;
import org.eclipse.emt4j.common.fileformat.VariableHeader;
import org.eclipse.emt4j.common.util.ClassURL;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

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

    public void execute(ReportInputProvider reportInputProvider, Progress parentProgress, boolean disableExternalTool) throws IOException, ClassNotFoundException, InterruptedException, URISyntaxException {
        ClassURL.registerUrlProtocolHandler();
        log("Reading checking result.");
        new Progress(parentProgress, "Read dependency records").printTitle();
        List<BodyRecord> recordList = reportInputProvider.getRecords();

        if (!disableExternalTool) {
            log("Prepare for invoking external tools.");
            new Progress(parentProgress, "Prepare for external tools").printTitle();
            ExternalToolParam etp = prepareExternalToolParam(recordList, reportInputProvider.getHeader());
            String externalToolRoot = reportConfig.getExternalToolRoot();
            if (externalToolRoot != null) {
                Path root = Paths.get(externalToolRoot);
                // Each directory in external tool root is the home of one external tool
                List<URL> urls = new ArrayList<>();
                try {
                    Files.list(root).filter(p -> Files.isDirectory(p)).forEach(p -> {
                        // Add jar files in each external tool' directory to URL list.
                        try {
                            Files.list(p).filter(f -> f.getFileName().toString().endsWith(".jar")).forEach(f -> {
                                try {
                                    urls.add(f.toUri().toURL());
                                } catch (MalformedURLException e) {
                                    log(e);
                                }
                            });
                        } catch (IOException e) {
                            log(e);
                        }
                    });
                } catch (IOException e) {
                    log(e);
                }
                if (!urls.isEmpty()) {
                    URLClassLoader externalToolLoader = new URLClassLoader(urls.toArray(new URL[0]), this.getClass().getClassLoader());
                    Iterator<Tool> toolIterator = ServiceLoader.load(Tool.class, externalToolLoader).iterator();
                    List<Tool> externalTools = new ArrayList<>();
                    while (toolIterator.hasNext()) {
                        externalTools.add(toolIterator.next());
                    }
                    int externalToolSize = externalTools.size();
                    if (externalToolSize > 0) {
                        Progress runExternalProgress = new Progress(parentProgress, "There are " + externalToolSize + " external tools to run");
                        runExternalProgress.printTitle();
                        for (int i = 0; i < externalToolSize; i++) {
                            Tool tool = externalTools.get(i);
                            new Progress(parentProgress, "Run " + (i + 1) + "/" + externalToolSize + " external tool:" + tool.name()).printTitle();

                            if (tool instanceof ModifyReportTool) {
                                // ModifyReportTool can delete the existing records and add new ones.
                                try {
                                    recordList = ((ModifyReportTool) tool).run(recordList, etp, reportConfig, parentProgress);
                                } catch (ExternalToolFailException e) {
                                    new Progress(parentProgress, "Fail to run external tool:" + tool.name()).printTitle();
                                    e.printStackTrace();
                                }
                            } else {
                                // Other Tools only add new records.
                                recordList.addAll(tool.analysis(etp, reportConfig, parentProgress));
                            }
                        }
                    }
                }
            }
        }
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
            if (null == record.getCheckResult()) {
                continue;
            }
            String feature = record.getFeature();
            CheckResultContext checkResultContext = new CheckResultContext(record.getCheckResult(), record.getDependency());
            resultMap.computeIfAbsent(feature, i -> new ArrayList<>()).add(checkResultContext);
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

    public Render getRender() {
        return render;
    }

    private void log(String msg) {
        if (reportConfig.isVerbose()) {
            System.out.println(msg);
        }
    }

    private void log(Exception e) {
        if (reportConfig.isVerbose()) {
            e.printStackTrace();
        }
    }
}
