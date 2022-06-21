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
package org.eclipse.emt4j.common.rule;

import org.eclipse.emt4j.common.rule.model.ConfRuleItem;
import org.eclipse.emt4j.common.rule.model.ConfRules;
import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class TestConfRuleManager {

    @Test
    public void testLoad() throws SAXException, IOException, URISyntaxException {
        String[] features = new String[]{"default"};
        List<ConfRules> rules1 = ConfRuleFacade.load(new String[]{"default"}, new String[]{"agent"}, 8, 17);
        assertTrue(rules1.size() > 0);
        for (ConfRules confRules : rules1) {
            assertTrue(inArray(confRules.getFeature(), features));
        }

        for (ConfRules confRules : rules1) {
            for (ConfRuleItem confRuleItem : confRules.getRuleItems()) {
                assertTrue(confRuleItem.getSupportModes().stream().anyMatch("agent"::equals));
            }
        }
    }

    private boolean inArray(String toFind, String[] array) {
        for (String e : array) {
            if (e.equals(toFind)) {
                return true;
            }
        }
        return false;
    }

}
