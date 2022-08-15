package org.eclipse.emt4j.test.common;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Project {
    private final Artifact[] artifacts;
    private RunConf runConf;

    public Project(RunConf runConf, Artifact[] artifacts) {
        this.runConf = runConf;
        this.artifacts = artifacts;
    }

    public void build(ProjectWorkDir workDir) throws IOException {
        Map<String, Artifact> artifactMap = new HashMap<>();
        List<Artifact> sorted = Artifact.topoSort(artifacts, artifactMap);
        for (Artifact artifact : sorted) {
            artifact.build(workDir, artifactMap, runConf);
        }
    }
}
