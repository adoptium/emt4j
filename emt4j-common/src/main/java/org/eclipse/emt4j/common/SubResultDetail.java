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


import java.util.ArrayList;
import java.util.List;

/**
 * result detail for a given sub result code.
 * If the main result code doesn't have sub result code, there exists a default sub result code.
 */
public class SubResultDetail {
    private String subResultCode;
    private String briefDesc;
    private List<String> moreDetailDesc = new ArrayList<>();
    private List<String> howToFix = new ArrayList<>();
    private List<IssueContext> issueContextList = new ArrayList<>();

    public String getSubResultCode() {
        return subResultCode;
    }

    public void setSubResultCode(String subResultCode) {
        this.subResultCode = subResultCode;
    }

    public String getBriefDesc() {
        return briefDesc;
    }

    public void setBriefDesc(String briefDesc) {
        this.briefDesc = briefDesc;
    }

    public List<String> getMoreDetailDesc() {
        return moreDetailDesc;
    }

    public void setMoreDetailDesc(List<String> moreDetailDesc) {
        this.moreDetailDesc = moreDetailDesc;
    }

    public List<String> getHowToFix() {
        return howToFix;
    }

    public void setHowToFix(List<String> howToFix) {
        this.howToFix = howToFix;
    }

    public List<IssueContext> getIssueContextList() {
        return issueContextList;
    }

    public void setIssueContextList(List<IssueContext> issueContextList) {
        this.issueContextList = issueContextList;
    }
}
