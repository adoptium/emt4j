package org.eclipse.emt4j.test.common;

import java.io.File;
import java.io.IOException;

public class ProjectWorkDir {
    private final File base;
    private final File build;
    private final File playground;

    public ProjectWorkDir(String base) throws IOException {
        this.base = new File(base);
        this.build = new File(base + File.separator + "build");
        this.playground = new File(base + File.separator + "playground");
        initDirs();
    }

    private void initDirs() throws IOException {
        for (File f : new File[]{base, build, playground}) {
            if (!f.exists() && !f.mkdirs()) {
                throw new RuntimeException("Cannot mkdir: " + f.getAbsolutePath());
            }
        }
    }

    public File getBuild() {
        return build;
    }

    public File getPlayground() {
        return playground;
    }

}
