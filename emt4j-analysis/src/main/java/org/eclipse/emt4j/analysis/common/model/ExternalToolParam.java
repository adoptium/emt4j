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
package org.eclipse.emt4j.analysis.common.model;

import lombok.Data;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class ExternalToolParam {
    private String vmOption;
    private Set<File> classesOrJars = new HashSet<>();
    private String targetJdkHome;
    private int fromVersion;
    private int toVersion;
    private List<String> features;
}