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
package org.eclipse.emt4j.analysis.common.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Progress {
    private int min;
    private int max;
    private String title;
    private int level;
    private int sequence;
    private List<Progress> children = new ArrayList<>();

    public Progress(int level, int sequence, int min, int max, String desc) {
        this.min = min;
        this.max = max;
        this.level = level;
        this.sequence = sequence;
        this.title = formatTitle(level, sequence, desc);
    }

    private String formatTitle(int level, int sequence, String desc) {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < level; i++) {
            sb.append("  ");
        }
        sb.append(sequence).append(". ").append(desc);
        return sb.toString();
    }

    public Progress(Progress parent, int min, int max, String desc) {
        this.level = parent.level + 1;
        this.sequence = parent.children.size() + 1;
        this.min = min;
        this.max = max;
        this.title = formatTitle(level, sequence, desc);
        parent.children.add(this);
        System.out.println();
    }

    public Progress(Progress parent, String desc) {
        this.level = parent.level + 1;
        this.sequence = parent.children.size() + 1;
        this.title = formatTitle(level, sequence, desc);
        parent.children.add(this);
    }

    public Progress(int level, int sequence, String desc) {
        this.level = level;
        this.sequence = sequence;
        this.title = formatTitle(level, sequence, desc);
    }

    public void printTitle() {
        System.out.println(title);
    }

    public void printProgress(int value) {
        if (value < min || value > max) {
            throw new RuntimeException("Current value :" + value + " invalid!");
        }
        int progress = (int) ((100.0f * (value - min)) / (max - min));
        System.out.print(title + "[" + progress + "%]\r");
    }

    public void cleanProgress() {
        char[] cs = new char[title.length() + 7];
        Arrays.fill(cs, ' ');
        System.out.print(new String(cs));
        System.out.print("\r");
    }
}
