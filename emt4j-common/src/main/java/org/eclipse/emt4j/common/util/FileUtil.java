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
package org.eclipse.emt4j.common.util;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.emt4j.common.JdkMigrationException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public class FileUtil {
    public static List<String> readPlainTextFromResource(String resourcePath, boolean includeComment) {
        try {
            List<String> lines = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(Objects.requireNonNull(FileUtil.class.getResourceAsStream(resourcePath)), StandardCharsets.UTF_8))) {
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

    private static final List<String> ELF_EXTENSIONS = Arrays.asList("so", "dll", "dylib", "o");

    public enum FileType {
        Java, Jar, Class,
        Cfg,
        Dat,
        Other;
    }

    public static FileType fileType(String path) {
        String f = path.toLowerCase(), ext;
        if (Pattern.matches("^.*\\.so(\\..+)?$", f)) {
            ext = "so";
        } else {
            ext = FilenameUtils.getExtension(f);
        }
        switch (ext) {
            case "jar":
                return FileType.Jar;
            case "class":
                return FileType.Class;
            case "dat":
                return FileType.Dat;
            case "cfg":
                return FileType.Cfg;
        }
        return FileType.Other;
    }

    public static boolean isSameFile(File file1, File file2) {
        try {
            return file1.getCanonicalPath().equals(file2.getCanonicalPath());
        } catch (IOException e) {
            return false;
        }
    }
}
