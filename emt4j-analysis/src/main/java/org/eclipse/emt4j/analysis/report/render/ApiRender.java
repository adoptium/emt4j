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
import org.eclipse.emt4j.common.MainResultDetail;
import org.eclipse.emt4j.common.ReportConfig;
import org.eclipse.emt4j.common.rule.ConfRuleFacade;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ApiRender extends AbstractRender implements Render {

    private List<MainResultDetail> resultDetailList = new ArrayList<>();

    public ApiRender(ReportConfig config) {
        super(config);
    }

    @Override
    public void doRender(Map<String, List<CheckResultContext>> resultMap) {
        CategorizedCheckResult categorizedCheckResult = categorize(resultMap);
        for (String feature : categorizedCheckResult.getFeatures()) {
            String i18nBase = ConfRuleFacade.getFeatureI18nBase(feature);
            for (TreeMap<String, TreeMap<String, List<CheckResultContext>>> map : categorizedCheckResult.getResult().get(feature)) {
                convert(map, resultDetailList, i18nBase);
            }
        }
    }

    public List<MainResultDetail> getResultDetailList() {
        return resultDetailList;
    }
}
