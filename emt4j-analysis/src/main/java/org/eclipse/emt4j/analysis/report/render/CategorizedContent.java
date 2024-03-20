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
package org.eclipse.emt4j.analysis.report.render;

import java.util.ArrayList;
import java.util.List;

public class CategorizedContent {
    private String title;
    private int id;

    private int total;

    private final List<String> descriptions = new ArrayList<>();

    private List<Content> subContents = new ArrayList<>();

    public CategorizedContent(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Content> getSubContents() {
        return subContents;
    }

    public void addSubContent(Content content) {
        subContents.add(content);
    }

    public void addTotal(int n) {
        total += n;
    }

    public void setSubContents(List<Content> subContents) {
        this.subContents = subContents;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTotal() {
        return total;
    }

    public List<String> getDescriptions() {
        return descriptions;
    }

    public void addDescription(String desc) {
        descriptions.add(desc);
    }
}
