package org.eclipse.emt4j.test.common;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.Attributes;
import java.util.stream.Collectors;

public class JarUtil {
    private static final String FARJAR_LIB_PATH = "BOOT-INF/lib/";
    private static final String FATJAR_CLASSES_PATH = "BOOT-INF/classes/";

    public static String writePlainJar(String destJarFile, File classes, ArtifactOption[] options) throws IOException {
        JarCreator jarCreator;
        if (options != null && Arrays.asList(options).contains(ArtifactOption.NO_MANIFEST)) {
            jarCreator = new JarCreator(destJarFile);
        } else {
            Map<Attributes.Name, String> manifestMap = new HashMap<>();
            manifestMap.put(Attributes.Name.MANIFEST_VERSION, "1.0.0");
            jarCreator = new JarCreator(destJarFile, manifestMap);
        }
        List<Path> classFiles = Files.walk(classes.toPath())
                .filter(Files::isRegularFile).collect(Collectors.toList());
        for (Path classFile : classFiles) {
            jarCreator.addFile(classes.getCanonicalPath(), classFile.toFile().getCanonicalPath());
        }
        jarCreator.close();
        return destJarFile;
    }

    public static String writeFatJar(String mainClass, String destJarFile, File outerClasses, List<File> innerFiles, ArtifactOption[] options) throws IOException {
        JarCreator jarCreator;
        if (options != null && Arrays.asList(options).contains(ArtifactOption.NO_MANIFEST)) {
            jarCreator = new JarCreator(destJarFile);
        } else {
            Map<Attributes.Name, String> manifestMap = new HashMap<>();
            manifestMap.put(Attributes.Name.MANIFEST_VERSION, "1.0.0");
            manifestMap.put(Attributes.Name.MAIN_CLASS, JavaSource.FATJAR_TRAMPOLINE_CLASSNAME);
            manifestMap.put(new Attributes.Name("Start-Class"), mainClass);
            jarCreator = new JarCreator(destJarFile, manifestMap);
        }
        jarCreator.addDirectoryEntry(FARJAR_LIB_PATH);
        jarCreator.addDirectoryEntry(FATJAR_CLASSES_PATH);
        List<Path> classFiles = Files.walk(outerClasses.toPath())
                .filter(Files::isRegularFile).collect(Collectors.toList());
        for (Path classFile : classFiles) {
            jarCreator.addFile(outerClasses.getCanonicalPath(), classFile.toFile().getCanonicalPath());
        }

        //3.2 add inner artifacts
        for (File inner : innerFiles) {
            if (inner.isDirectory()) {
                classFiles = Files.walk(inner.toPath()).filter(Files::isRegularFile).collect(Collectors.toList());
                for (Path classFile : classFiles) {
                    jarCreator.addFile(() -> FATJAR_CLASSES_PATH + getRelativePath(inner.getAbsolutePath(), classFile.toFile().getAbsolutePath()),
                            classFile.toFile().getAbsolutePath());
                }

            } else if (inner.getName().endsWith(".jar")) {
                jarCreator.addFile(() -> FARJAR_LIB_PATH + inner.getName(), inner.getAbsolutePath());
            } else {
                throw new RuntimeException("Fat jar only support contain classes and plain jar.but now is " + inner.getAbsolutePath());
            }
        }
        jarCreator.close();
        return destJarFile;
    }

    private static String getRelativePath(String rootPath, String fullPath) {
        if (rootPath.endsWith(File.separator)) {
            return fullPath.substring(rootPath.length());
        } else {
            return fullPath.substring(rootPath.length() + 1);
        }
    }
}
