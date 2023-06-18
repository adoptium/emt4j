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

import org.eclipse.emt4j.common.Feature;
import org.eclipse.emt4j.common.rule.model.ConfRules;
import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.Assert.*;

public class TestConfRuleRepository {
    @Test
    public void testLoad() throws SAXException, IOException, URISyntaxException {
        ConfRules confRules = ConfRuleRepository.load(Feature.DEFAULT, 8, 11).get();
        assertNotNull(confRules);
        assertEquals(confRules.getFromVersion(), 8);
        assertEquals(confRules.getToVersion(), 11);
        assertEquals(confRules.getFeature(), Feature.DEFAULT);
        assertNotNull(confRules.getRuleDataPathPrefix());
        assertFalse(confRules.getRuleItems().isEmpty());
        assertTrue(confRules.getRuleItems().stream().anyMatch((r) -> r.getType().equals("reference-class")));
    }
}
