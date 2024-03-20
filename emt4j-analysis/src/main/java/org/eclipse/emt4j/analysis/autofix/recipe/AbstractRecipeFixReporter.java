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
package org.eclipse.emt4j.analysis.autofix.recipe;

import org.eclipse.emt4j.analysis.autofix.AutofixReport;
import org.eclipse.emt4j.analysis.autofix.AutofixReport.FixedInfo;
import org.openrewrite.SourceFile;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class AbstractRecipeFixReporter implements RecipeFixReporter {
    private final String type;
    private final String[] i18nParams;

    public AbstractRecipeFixReporter(String type) {
        this(type, null);
    }

    public AbstractRecipeFixReporter(String type, String[] i18nParams) {
        this.type = type;
        this.i18nParams = i18nParams;
        AutofixReport.getInstance().addRecipeFixReporter(this);
    }

    public String getType() {
        return type;
    }

    public String[] getI18nParams() {
        return i18nParams;
    }

    // fix of this recipe will not be reported (maybe because it has been covered by java or dependency part of report)
    public static class NoOpRecipeFixReporter extends AbstractRecipeFixReporter {
        public NoOpRecipeFixReporter() {
            super(null);
        }

        @Override
        public void recordModification(SourceFile file) {
        }

        @Override
        public FixedInfo getFixedInfo() {
            return null;
        }
    }

    // regardless of how many files were modified, the result is recorded as 1, and files are reported
    public static class CountAsOneProblemRecipeFixReporter extends AbstractRecipeFixReporter {
        private Set<String> files = new HashSet<>();

        public CountAsOneProblemRecipeFixReporter(String type) {
            super(type);
        }

        public CountAsOneProblemRecipeFixReporter(String type, String[] i18nParams) {
            super(type, i18nParams);
        }

        @Override
        public void recordModification(SourceFile file) {
            files.add(file.getSourcePath().toFile().getAbsolutePath());
        }

        @Override
        public FixedInfo getFixedInfo() {
            return files.size() == 0 ? null : new FixedInfo(getType(), getI18nParams(), 1, files);
        }
    }

    // regardless of how many files were modified, the result is recorded as 1. Fixed file are not reported
    public static class CountAsOneNoFileProblemRecipeFixReporter extends AbstractRecipeFixReporter {
        private boolean fixed = false;

        public CountAsOneNoFileProblemRecipeFixReporter(String type) {
            super(type);
        }

        public CountAsOneNoFileProblemRecipeFixReporter(String type, String[] i18nParams) {
            super(type, i18nParams);
        }

        @Override
        public void recordModification(SourceFile file) {
            fixed = true;
        }

        @Override
        public FixedInfo getFixedInfo() {
            return !fixed ? null : new FixedInfo(getType(), getI18nParams(), 1, null);
        }
    }

    // fixed count is equal to unique files modified by the recipe
    public static class CountByFileRecipeFixReporter extends AbstractRecipeFixReporter {
        private Set<String> files = new HashSet<>();

        public CountByFileRecipeFixReporter(String type) {
            super(type);
        }

        public CountByFileRecipeFixReporter(String type, String[] i18nParams) {
            super(type, i18nParams);
        }

        @Override
        public void recordModification(SourceFile file) {
            files.add(file.getSourcePath().toFile().getAbsolutePath());
        }

        @Override
        public FixedInfo getFixedInfo() {
            return files.size() == 0 ? null : new FixedInfo(getType(), getI18nParams(), files.size(), files);
        }
    }

    // Fix count is always one, and the caller guarantee modification must have taken place.
    // Fixed files are not reported;
    public static class AlwaysOneProblemRecipeFixReporter extends AbstractRecipeFixReporter {
        public AlwaysOneProblemRecipeFixReporter(String type) {
            super(type);
        }

        public AlwaysOneProblemRecipeFixReporter(String type, String[] i18nParams) {
            super(type, i18nParams);
        }

        @Override
        public void recordModification(SourceFile file) {
        }

        @Override
        public FixedInfo getFixedInfo() {
            return new FixedInfo(getType(), getI18nParams(), 1, Collections.emptyList());
        }
    }
}
