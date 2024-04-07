/********************************************************************************
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
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
package org.eclipse.emt4j.plugin;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.eclipse.emt4j.analysis.autofix.AutofixConfig;
import org.eclipse.emt4j.analysis.autofix.BaseAutofixExecutor;
import org.eclipse.emt4j.analysis.autofix.FullAutofixExecutor;

/**
 * Find the incompatible issues existing in the project and tries to autofix them.
 */
@Mojo(name = "process", defaultPhase = LifecyclePhase.PROCESS_TEST_CLASSES, requiresDependencyResolution = ResolutionScope.TEST)
public class CheckAndAutofixMojo extends BaseCheckAndAutofixMojo {
    protected BaseAutofixExecutor getAutofixExecutor() {
        return new FullAutofixExecutor(AutofixConfig.getInstance(), session, project, getLog(), runtime, settingsDecrypter);
    }
}
