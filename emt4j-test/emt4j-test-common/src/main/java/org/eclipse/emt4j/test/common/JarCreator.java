package org.eclipse.emt4j.test.common;

import java.io.*;
import java.util.Map;
import java.util.function.Supplier;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

public class JarCreator {
    private Manifest manifest = new Manifest();

    private JarOutputStream outputStream;

    public JarCreator(String jarFile) throws IOException {
        prepareParentDir(jarFile);
        this.outputStream = new JarOutputStream(new FileOutputStream(jarFile));
    }

    private void prepareParentDir(String jarFile) {
        File f = new File(jarFile);
        if (!f.getParentFile().exists() && !f.getParentFile().mkdirs()) {
            throw new RuntimeException("Create parent dir for file " + f + " failed!");
        }
    }

    public JarCreator(String jarFile, Map<Attributes.Name, String> manifestMap) throws IOException {
        prepareParentDir(jarFile);
        if (manifestMap != null) {
            manifestMap.forEach((k, v) -> manifest.getMainAttributes().put(k, v));
        }
        this.outputStream = new JarOutputStream(new FileOutputStream(jarFile), manifest);
    }

    public void addFile(String rootPath, String source) throws IOException {
        String remaining = "";
        if (rootPath.endsWith(File.separator)) {
            remaining = source.substring(rootPath.length());
        } else {
            remaining = source.substring(rootPath.length() + 1);
        }
        String name = remaining.replace("\\", "/");
        JarEntry entry = new JarEntry(name);
        entry.setTime(new File(source).lastModified());
        outputStream.putNextEntry(entry);

        BufferedInputStream in = new BufferedInputStream(new FileInputStream(source));
        byte[] buffer = new byte[1024];
        while (true) {
            int count = in.read(buffer);
            if (count == -1) {
                break;
            }
            outputStream.write(buffer, 0, count);
        }
        outputStream.closeEntry();
        in.close();
    }

    public void addDirectoryEntry(String entryName) throws IOException {
        JarEntry entry = new JarEntry(entryName);
        outputStream.putNextEntry(entry);
        outputStream.closeEntry();
    }

    public void addFile(Supplier<String> nameProvider, String source) throws IOException {
        JarEntry entry = new JarEntry(nameProvider.get());
        entry.setTime(new File(source).lastModified());
        outputStream.putNextEntry(entry);

        BufferedInputStream in = new BufferedInputStream(new FileInputStream(source));
        byte[] buffer = new byte[1024];
        while (true) {
            int count = in.read(buffer);
            if (count == -1) {
                break;
            }
            outputStream.write(buffer, 0, count);
        }
        outputStream.closeEntry();
        in.close();
    }

    public void close() throws IOException {
        outputStream.flush();
        outputStream.close();
    }
}
