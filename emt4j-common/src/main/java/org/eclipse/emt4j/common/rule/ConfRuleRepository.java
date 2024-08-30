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
import org.eclipse.emt4j.common.IssuePriority;
import org.eclipse.emt4j.common.JdkMigrationException;
import org.eclipse.emt4j.common.rule.model.ConfRuleItem;
import org.eclipse.emt4j.common.rule.model.ConfRules;
import org.eclipse.emt4j.common.util.MutableBoolean;
import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;


/**
 * Load rule files in classpath by feature.
 * It read the rule.xml and validate it,then convert it to <code>ConfRules</code>
 */
public class ConfRuleRepository {
    public static Optional<ConfRules> load(Feature feature, int fromVersion, int toVersion) throws URISyntaxException, IOException, SAXException {
        if (null == feature) {
            throw new RuntimeException("feature cannot be null!");
        }

        String basePath = feature.getRuleBasePath(fromVersion, toVersion);
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = schemaFactory.newSchema(ConfRuleRepository.class.getResource("/xsd/rules.xsd").toURI().toURL());
        Validator validator = schema.newValidator();
        MutableBoolean error = new MutableBoolean();
        validator.setErrorHandler(new ErrorHandler() {
            @Override
            public void warning(SAXParseException exception) throws SAXException {
                exception.printStackTrace();
                error.setValue(true);
            }

            @Override
            public void error(SAXParseException exception) throws SAXException {
                exception.printStackTrace();
                error.setValue(true);
            }

            @Override
            public void fatalError(SAXParseException exception) throws SAXException {
                exception.printStackTrace();
                error.setValue(true);
            }
        });

        String rulePath = basePath + "/rule.xml";
        try (InputStream is = ConfRuleRepository.class.getResourceAsStream(rulePath)) {
            if (is == null) {
                return Optional.empty();
            }
            validator.validate(new StreamSource(is));
            if (error.isValue()) {
                throw new JdkMigrationException("XSD validation failed for file: " + rulePath);
            }
        }

        try (InputStream is = ConfRuleRepository.class.getResourceAsStream(rulePath)) {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            RuleConfSAXHandler ruleConfSAXHandler = new RuleConfSAXHandler();
            saxParser.parse(is, ruleConfSAXHandler);

            ConfRules confRules = new ConfRules();
            confRules.setRuleDataPathPrefix(basePath + "/data/");
            confRules.setFromVersion(fromVersion);
            confRules.setToVersion(toVersion);
            confRules.setFeature(feature);
            confRules.setRuleItems(ruleConfSAXHandler.ruleItems);
            return Optional.of(confRules);
        } catch (IOException | SAXException | ParserConfigurationException e) {
            throw new RuntimeException("Cannot found rule config for feature:" + feature + ",fromVersion:" + fromVersion + ",toVersion:" + toVersion, e);
        }
    }

    private static class RuleConfSAXHandler extends DefaultHandler {
        private StringBuilder currentValue = new StringBuilder();
        List<ConfRuleItem> ruleItems = new ArrayList<>();
        private ConfRuleItem confRuleItem;

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            currentValue.setLength(0);
            if (qName.equals("rule")) {
                confRuleItem = new ConfRuleItem();
                confRuleItem.setDesc(attributes.getValue("desc"));
                confRuleItem.setType(attributes.getValue("type"));
                confRuleItem.setResultCode(attributes.getValue("result-code"));
                confRuleItem.setSubResultCode(attributes.getValue("sub-result-code"));
                confRuleItem.setPriority(IssuePriority.toIntPriority(attributes.getValue("priority")));
                List<String[]> userDefineAttrs = new ArrayList<>(attributes.getLength());
                for (int i = 0; i < attributes.getLength(); i++) {
                    String attrName = attributes.getQName(i);
                    if (!isFixedAttribute(attrName)) {
                        userDefineAttrs.add(new String[]{attrName, attributes.getValue(i)});
                    }
                }
                confRuleItem.setUserDefineAttrs(userDefineAttrs);
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (qName.equals("rule")) {
                ruleItems.add(confRuleItem);
            }
            if (qName.equals("mode")) {
                confRuleItem.getSupportModes().add(currentValue.toString());
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            currentValue.append(ch, start, length);
        }

        private boolean isFixedAttribute(String attrName) {
            return Arrays.stream(FIXED_ATTR_NAME).anyMatch(attrName::equals);
        }
    }

    private static final String[] FIXED_ATTR_NAME = new String[]{
            "desc", "type", "result-code", "sub-result-code", "support-modes", "priority"
    };
}
