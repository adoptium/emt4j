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
package org.eclipse.emt4j.common.util;

import org.eclipse.emt4j.common.JdkMigrationException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class FileUtil {
    public static List<String> readPlainTextFromResource(String resourcePath, boolean includeComment) {
        try {
            List<String> lines = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(FileUtil.class.getResourceAsStream(resourcePath), StandardCharsets.UTF_8))) {
                String line = br.readLine();
                while (line != null) {
                    if (!line.startsWith("#") || includeComment) {
                        lines.add(line);
                    }
                    line = br.readLine();
                }
            }
            return lines;
        } catch (Exception e) {
            throw new JdkMigrationException("Read resource failed by path:" + resourcePath, e);
        }
    }
}
