package org.eclipse.emt4j.test.common;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public interface RunConf {
    String[] buildJavaRunCommands(File buildDir, Artifact[] artifacts);

    default List<String> classpath(File buildDir, Artifact[] artifacts) {
        if (artifacts != null && artifacts.length > 0) {
            List<String> paths = new ArrayList<>();
            for (Artifact artifact : artifacts) {
                paths.add(new File(buildDir, artifact.getFileRelativePath()).getAbsolutePath());
            }
            return paths;
        } else {
            return null;
        }
    }

    String mainClass();
}
