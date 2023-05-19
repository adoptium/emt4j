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
package org.eclipse.emt4j.analysis.report;

import org.eclipse.emt4j.analysis.common.util.Option;
import org.eclipse.emt4j.analysis.common.util.OptionProcessor;
import org.eclipse.emt4j.analysis.common.util.Progress;
import org.eclipse.emt4j.common.ReportConfig;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Report's main work is to convert from an immediate problem file to a report file.
 */
public class ReportMain {
    private static final String DEFAULT_REPORT_FILE = "report";
    private static final String DEFAULT_FORMAT = "html";

    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException, URISyntaxException {
        if (null == args || args.length == 0) {
            printUsage(null);
        }

        ReportConfig reportConfig = new ReportConfig();
        OptionProcessor optionProcessor = new OptionProcessor(args);
        optionProcessor.addOption(Option.buildParamWithValueOption("-i", (v) -> new File(v).exists(), (v) -> reportConfig.getInputFiles().add(new File(v))));
        optionProcessor.addOption(Option.buildParamWithValueOption("-f",
                (v) -> "txt".equalsIgnoreCase(v) || "json".equalsIgnoreCase(v) || "html".equalsIgnoreCase(v), (v) -> reportConfig.setOutputFormat(v.toLowerCase())));
        optionProcessor.addOption(Option.buildParamWithValueOption("-o", null, reportConfig::setOutputFile));
        optionProcessor.addOption(Option.buildParamWithValueOption("-t", (v) -> new File(v).exists()
                        && new File(v).isDirectory(), reportConfig::setTargetJdkHome));
        optionProcessor.addOption(Option.buildParamNoValueOption("-v", null, (v) -> reportConfig.setVerbose(true)));
        optionProcessor.setShowUsage(ReportMain::printUsage);
        optionProcessor.process();

        Progress progress = new Progress(1, 2, 1, 100, "Report");
        run(reportConfig, progress);
    }

    public static void run(ReportConfig reportConfig, Progress progress) throws InterruptedException, IOException, ClassNotFoundException, URISyntaxException {
        resolveOutputFormat(reportConfig);
        if (reportConfig.getOutputFile() == null) {
            reportConfig.setOutputFile(DEFAULT_REPORT_FILE + '.' + reportConfig.getOutputFormat());
        }
        ReportExecutor reportExecutor = new ReportExecutor(reportConfig);
        progress.printTitle();
        reportExecutor.execute(new BinaryFileInputProvider(reportConfig.getInputFiles()), progress, false);
        System.out.println("EMT4J's report: " + reportConfig.getOutputFile());
    }

    private static void resolveOutputFormat(ReportConfig config) {
        if (config.getOutputFormat() == null) {
            String outputFile = config.getOutputFile();
            config.setOutputFormat(DEFAULT_FORMAT);
            if (outputFile == null) {
                return;
            }
            String lowerCase= outputFile.toLowerCase();
            if (lowerCase.endsWith(".txt")) {
                config.setOutputFormat("txt");
            } else if (lowerCase.endsWith(".json")) {
                config.setOutputFormat("json");
            } else if (lowerCase.endsWith(".html")) {
                config.setOutputFormat("html");
            }
        }
    }

    private static void printUsage(String option) {
        if (option != null) {
            System.err.println(option + " is invalid!");
        }
        System.err.println("Usage:report.sh -i input file1 -i input file2  [-f txt|json|html] [-o output file] [-l language] [-t target jdk home] [-v]");
        System.err.println("-i input-file. The input file is the output of analysis or agent.");
        System.err.println("-f txt or json or html(default).");
        System.err.println("-o Write report to output file instead of default " + DEFAULT_REPORT_FILE + "." + DEFAULT_FORMAT);
        System.err.println("-t target jdk home.Provide target jdk home can help to find more compatible problems.");
        System.err.println("-v Show verbose information.");
        System.exit(1);
    }
}
