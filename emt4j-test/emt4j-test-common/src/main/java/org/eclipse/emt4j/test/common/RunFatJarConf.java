package org.eclipse.emt4j.test.common;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class RunFatJarConf implements RunConf {

    private final String mainClass;

    public RunFatJarConf(String mainClass) {
        this.mainClass = mainClass;
    }

    @Override
    public String[] buildJavaRunCommands(File buildDir, Artifact[] artifacts) {
        List<String> commands = new ArrayList<>();
        commands.add(JavaSource.FATJAR_TRAMPOLINE_CLASSNAME);
        return commands.toArray(new String[commands.size()]);
    }

    @Override
    public String mainClass() {
        return mainClass;
    }
}
