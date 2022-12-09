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
package org.eclipse.emt4j.analysis.api;

import org.eclipse.emt4j.analysis.common.model.CheckTargetTypeEnum;
import org.eclipse.emt4j.analysis.common.model.JdkCheckCompatibleRequest;
import org.eclipse.emt4j.analysis.common.model.JdkCheckCompatibleResult;
import org.eclipse.emt4j.analysis.common.model.ToCheckTarget;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.*;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestJdkCompatibleCheckFacade {

    @Test
    public void testCheck() throws IOException, ClassNotFoundException, InterruptedException, URISyntaxException {
        File jarFile = createJar();
        JdkCheckCompatibleRequest request = new JdkCheckCompatibleRequest();
        request.setFromVersion(8);
        request.setToVersion(11);
        request.setReportLocale(Locale.CHINA);
        request.setExternalToolHome(System.getProperty("user.home") + "/emt4j-external");
        List<ToCheckTarget> toCheckTargetList = new ArrayList<>();
        ToCheckTarget toCheckTarget = new ToCheckTarget();
        toCheckTarget.setTargetType(CheckTargetTypeEnum.JAR);
        toCheckTarget.setTargetIdentifier(jarFile.getCanonicalPath());
        toCheckTargetList.add(toCheckTarget);
        request.setToCheckTargetList(toCheckTargetList);

        JdkCheckCompatibleResult result = JdkCompatibleCheckFacade.check(request);
        assertTrue(result != null);
        assertFalse(result.getResultDetailList().isEmpty());

        jarFile.delete();
    }

    private File createJar() throws IOException {
        try (InputStream inputStream = TestJdkCompatibleCheckFacade.class.getResourceAsStream("/commons-lang-2.6.testfile")) {
            File jarFile = File.createTempFile("commons-lang-2.6", ".jar");
            IOUtils.copy(inputStream, new FileOutputStream(jarFile));
            return jarFile;
        }
    }
}
