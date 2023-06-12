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

import org.eclipse.emt4j.common.CheckResultContext;
import org.eclipse.emt4j.common.Feature;

import java.util.*;

public class CategorizedCheckResult {

    /**
     * <feature,<result code,<sub result code>>
     */
    private Map<Feature, List<TreeMap<String, TreeMap<String, List<CheckResultContext>>>>> result = new HashMap<>();

    private List<Feature> features = new ArrayList<>();

    public Map<Feature, List<TreeMap<String, TreeMap<String, List<CheckResultContext>>>>> getResult() {
        return result;
    }

    public List<Feature> getFeatures() {
        return features;
    }

    public boolean noResult() {
        if (!result.isEmpty()) {
            for (Feature feature : features) {
                if (!result.get(feature).isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }
}
