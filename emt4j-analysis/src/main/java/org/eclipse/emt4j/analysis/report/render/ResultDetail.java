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
package org.eclipse.emt4j.analysis.report.render;

import org.eclipse.emt4j.common.IssueContext;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ResultDetail {
    int detailId;
    String mainResultCode;
    String subResultCode;
    String title;
    String descriptionTitle;
    String description;
    String solutionTitle;
    List<String> solution = new ArrayList<>();
    String contextTitle;
    Set<IssueContext> context = new HashSet<>();
    String anchorId;

    public int getDetailId() {
        return detailId;
    }

    public void setDetailId(int detailId) {
        this.detailId = detailId;
    }

    public String getMainResultCode() {
        return mainResultCode;
    }

    public void setMainResultCode(String mainResultCode) {
        this.mainResultCode = mainResultCode;
    }

    public String getSubResultCode() {
        return subResultCode;
    }

    public void setSubResultCode(String subResultCode) {
        this.subResultCode = subResultCode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescriptionTitle() {
        return descriptionTitle;
    }

    public void setDescriptionTitle(String descriptionTitle) {
        this.descriptionTitle = descriptionTitle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSolutionTitle() {
        return solutionTitle;
    }

    public void setSolutionTitle(String solutionTitle) {
        this.solutionTitle = solutionTitle;
    }

    public List<String> getSolution() {
        return solution;
    }

    public void setSolution(List<String> solution) {
        this.solution = solution;
    }

    public String getContextTitle() {
        return contextTitle;
    }

    public void setContextTitle(String contextTitle) {
        this.contextTitle = contextTitle;
    }

    public Set<IssueContext> getContext() {
        return context;
    }

    public void setContext(Set<IssueContext> context) {
        this.context = context;
    }

    public String getAnchorId() {
        return anchorId;
    }

    public void setAnchorId(String anchorId) {
        this.anchorId = anchorId;
    }
}
