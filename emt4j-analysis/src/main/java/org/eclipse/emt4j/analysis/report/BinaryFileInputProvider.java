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
package org.eclipse.emt4j.analysis.report;

import org.eclipse.emt4j.analysis.common.ReportInputProvider;
import org.eclipse.emt4j.common.JdkMigrationException;
import org.eclipse.emt4j.common.fileformat.BodyRecord;
import org.eclipse.emt4j.common.fileformat.FixedHeader;
import org.eclipse.emt4j.common.fileformat.VariableHeader;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class BinaryFileInputProvider implements ReportInputProvider {
    private VariableHeader vh;
    private List<BodyRecord> recordList = new ArrayList<>();
    private final List<File> files;
    private volatile boolean init = false;

    public BinaryFileInputProvider(List<File> files) {
        this.files = files;
    }

    @Override
    public List<BodyRecord> getRecords() throws IOException, ClassNotFoundException {
        init();
        return recordList;
    }

    void init() throws IOException, ClassNotFoundException {
        if (!init) {
            synchronized (BinaryFileInputProvider.class) {
                if (!init) {
                    readTmpFile(files);
                }
            }
        }
    }

    @Override
    public VariableHeader getHeader() throws IOException, ClassNotFoundException {
        init();
        return this.vh;
    }

    private void readTmpFile(List<File> files) throws IOException, ClassNotFoundException {
        for (int i = 0; i < files.size(); i++) {
            File inputFile = files.get(i);
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(inputFile))) {
                FixedHeader fh = (FixedHeader) ois.readObject();
                if (fh.getMagic() != FixedHeader.MAGIC || fh.getVersion() != FixedHeader.VERSION) {
                    throw new JdkMigrationException("Not a valid file that you provided " + inputFile + ",it should generate by analysis or agent!");
                }
                VariableHeader vh = (VariableHeader) ois.readObject();
                if (this.vh != null) {
                    checkHeader(this.vh, vh, files.get(0), files.get(i));
                } else {
                    this.vh = vh;
                }

                try {
                    BodyRecord br = (BodyRecord) ois.readObject();
                    while (br != null) {
                        recordList.add(br);
                        br = (BodyRecord) ois.readObject();
                    }
                } catch (EOFException e) {
                }
            }
        }

    }

    private void checkHeader(VariableHeader a, VariableHeader b, File afile, File bfile) {
        if (a.getFromVersion() != b.getFromVersion() || a.getToVersion() != b.getToVersion()) {
            throw new JdkMigrationException("The fromVersion or toVersion of variable headers are different!" +
                    "The file:" + afile + " header is : " + a + ",the other file " + bfile + " header is : " + b);
        }
    }
}
