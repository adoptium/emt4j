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
package org.eclipse.emt4j.test.common;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Parse a <a href="https://maven.apache.org/guides/mini/guide-using-toolchains.html"> toolchain </a> file.
 * emt4j need to use the different JDK to run the SIT test case.
 * But toolchains can only use in maven plugin, we reuse the toolchains configuration file.
 */
public class ToolchainConfReader {
    static Map<String, String> parseToolChainsConfig(String toolChainFile) {
        File file = new File(toolChainFile);
        if (!file.exists()) {
            throw new RuntimeException("Toolchain configure file " + toolChainFile + " not exist!");
        }
        try (InputStream is = new FileInputStream(file)) {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            ToolchainSAXHandler saxHandler = new ToolchainSAXHandler();
            saxParser.parse(is, saxHandler);
            return saxHandler.jdkConfList.stream().filter((c) -> c.getType().equals("jdk")).collect(
                    Collectors.toMap(JdkConf::getVersion, JdkConf::getJdkHome));
        } catch (IOException | SAXException | ParserConfigurationException e) {
            throw new RuntimeException("Read tool chains conf file :" + toolChainFile + " exception!", e);
        }
    }

    private static class ToolchainSAXHandler extends DefaultHandler {
        private StringBuilder currentValue = new StringBuilder();
        List<JdkConf> jdkConfList = new ArrayList<>();
        private JdkConf currJdkConf;

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            currentValue.setLength(0);
            if (qName.equals("toolchain")) {
                currJdkConf = new JdkConf();
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (qName.equals("type")) {
                currJdkConf.setType(currentValue.toString());
            } else if (qName.equals("version")) {
                currJdkConf.setVersion(currentValue.toString());
            } else if (qName.equals("vendor")) {
                currJdkConf.setVendor(currentValue.toString());
            } else if (qName.equals("jdkHome")) {
                currJdkConf.setJdkHome(currentValue.toString());
            } else if (qName.equals("toolchain")) {
                jdkConfList.add(currJdkConf);
                currJdkConf = null;
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            currentValue.append(ch, start, length);
        }
    }
}
