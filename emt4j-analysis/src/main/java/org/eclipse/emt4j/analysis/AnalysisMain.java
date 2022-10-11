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
package org.eclipse.emt4j.analysis;

import org.eclipse.emt4j.analysis.common.util.Progress;
import org.eclipse.emt4j.analysis.out.BinaryFileOutputConsumer;
import org.eclipse.emt4j.analysis.report.ReportMain;
import org.eclipse.emt4j.analysis.common.util.Option;
import org.eclipse.emt4j.analysis.common.util.OptionProcessor;
import org.eclipse.emt4j.analysis.source.*;
import org.eclipse.emt4j.common.CheckConfig;
import org.eclipse.emt4j.common.Feature;
import org.eclipse.emt4j.common.ReportConfig;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


/**
 * The main entry of class analysis and report
 */
public class AnalysisMain {
    private static final String DEFAULT_FILE = "analysis_output";

    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException, URISyntaxException {
        Progress root = new Progress(0, 0, "ROOT");
        doReport(doAnalysis(args, new Progress(root, "Analysis")),
                new Progress(root, "Report"));
    }

    private static void doReport(ReportConfig reportConfig, Progress progress) throws InterruptedException, IOException, ClassNotFoundException, URISyntaxException {
        ReportMain.run(reportConfig, progress);
    }

    static ReportConfig doAnalysis(String[] args, Progress progress) throws IOException {
        if (null == args || args.length == 0) {
            printUsage(null);
        }

        progress.printTitle();
        CheckConfig checkConfig = new CheckConfig();
        ReportConfig reportConfig = new ReportConfig();
        final List<Feature> featureList = new ArrayList<>();
        AnalysisExecutor analysisExecutor = new AnalysisExecutor(checkConfig);
        OptionProcessor optionProcessor = new OptionProcessor(args);
        optionProcessor.addOption(Option.buildParamWithValueOption("-o", null, (v) -> reportConfig.setOutputFile(v)));
        optionProcessor.addOption(Option.buildParamWithValueOption("-f", (v) -> StringUtils.isNumeric(v), (v) -> checkConfig.setFromVersion(Integer.valueOf(v))));
        optionProcessor.addOption(Option.buildParamWithValueOption("-t", (v) -> StringUtils.isNumeric(v), (v) -> checkConfig.setToVersion(Integer.valueOf(v))));
        optionProcessor.addOption(Option.buildParamWithValueOption("-p",
                (v) -> "txt".equalsIgnoreCase(v) || "json".equalsIgnoreCase(v) || "html".equalsIgnoreCase(v), (v) -> reportConfig.setOutputFormat(v.toLowerCase())));
        optionProcessor.addOption(Option.buildParamWithValueOption("-j", (v) -> new File(v).exists()
                && new File(v).isDirectory(), (v) -> reportConfig.setTargetJdkHome(v)));
        optionProcessor.addOption(Option.buildParamNoValueOption("-v", null, (v) -> {
            checkConfig.setVerbose(true);
            reportConfig.setVerbose(true);
        }));
        optionProcessor.addOption(Option.buildDefaultOption((v) -> getSource(v).isPresent(),
                (v) -> {
                    DependencySource ds = getSource(v).get();
                    if (ds.needAnalysis()) {
                        analysisExecutor.add(ds);
                    } else {
                        reportConfig.getInputFiles().add(ds.getFile());
                    }
                }));
        optionProcessor.setShowUsage((s) -> printUsage(s));
        if (checkConfig.getFromVersion() >= checkConfig.getToVersion()) {
            printUsage("from version should less than to version");
        }

        optionProcessor.process();
        if (featureList.isEmpty()) {
            featureList.add(Feature.DEFAULT);
        }

        if (analysisExecutor.hasSource()) {
            File tempFile = File.createTempFile(DEFAULT_FILE, ".dat");
            tempFile.deleteOnExit();
            try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(tempFile))) {
                analysisExecutor.setAnalysisOutputConsumer(new BinaryFileOutputConsumer(out));
                analysisExecutor.execute(featureList, progress);
            }
            reportConfig.getInputFiles().add(tempFile);
            System.out.println("Write analysis temporary file to " + tempFile + " done.");
        }
        if (reportConfig.getInputFiles().isEmpty()) {
            printUsage(null);
        }

        return reportConfig;
    }

    private static Optional<DependencySource> getSource(String file) {
        File f = new File(file);
        if (!f.exists()) {
            System.err.println(file + " not exist!");
            return Optional.empty();
        }

        if (f.isFile()) {
            if (file.endsWith(".class")) {
                return Optional.of(new SingleClassSource(f));
            } else if (file.endsWith(".cfg")) {
                return Optional.of(new JavaOptionSource(f));
            } else if (file.endsWith(".jar")) {
                return Optional.of(new SingleJarSource(f));
            } else if (file.endsWith(".dat")) {
                return Optional.of(new AgentOutputAsSource(f));
            }
        } else if (f.isDirectory()) {
            return Optional.of(new DirectorySource(f));
        }

        return Optional.empty();
    }

    private static void printUsage(String option) {
        if (option != null) {
            System.err.println(option + " is invalid!");
        }

        String osName = System.getProperty("os.name");
        boolean windows = osName != null && osName.toLowerCase().indexOf("windows") != -1;
        String launcher = windows ? "analysis.bat" : "analysis.sh";
        System.err.println("Usage:" + launcher + " [-f version] [-t version] [-p txt] [-o outputfile] [-j target jdk home] [-v] <files>");
        System.err.println("-f From which JDK version,default is 8");
        System.err.println("-t To which JDK version,default is 11");
        System.err.println("-p The report format.Can be TXT or JSON or HTML.Default is HTML");
        System.err.println("-o Write analysis to output file. Default is " + DEFAULT_FILE);
        System.err.println("-j target jdk home.Provide target jdk home can help to find more compatible problems.");
        System.err.println("-v Show verbose information.");
        System.err.println("files can be combination of following types :");
        final String[] allSupportFiles = new String[]{
                "Agent output file(*.dat)",
                "Single class file(*.class)",
                "Directory contains jars,classes",
                "File end with .cfg that contain java option(*.cfg)"
        };
        for (int i = 1; i <= allSupportFiles.length; i++) {
            System.err.println(i + ". " + allSupportFiles[i - 1]);
        }

        System.err.println("files can be class, jar,directory or java source file.");
        System.exit(1);
    }
}
