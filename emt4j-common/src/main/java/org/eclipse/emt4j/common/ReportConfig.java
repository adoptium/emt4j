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
package org.eclipse.emt4j.common;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ReportConfig {
    private List<File> inputFiles = new ArrayList<>();

    private String outputFile;

    private boolean verbose = false;
    private String outputFormat;

    private Locale locale = Locale.getDefault();

    private String targetJdkHome;

    public String getExternalToolRoot() {
        return externalToolRoot;
    }

    public void setExternalToolRoot(String externalToolRoot) {
        this.externalToolRoot = externalToolRoot;
    }

    private String externalToolRoot;

    public List<File> getInputFiles() {
        return inputFiles;
    }

    public void setInputFiles(List<File> inputFiles) {
        this.inputFiles = inputFiles;
    }

    public String getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(String outputFile) {
        this.outputFile = outputFile;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public String getOutputFormat() {
        return outputFormat;
    }

    public void setOutputFormat(String outputFormat) {
        this.outputFormat = outputFormat;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public String getTargetJdkHome() {
        return targetJdkHome;
    }

    public void setTargetJdkHome(String targetJdkHome) {
        this.targetJdkHome = targetJdkHome;
    }
}
