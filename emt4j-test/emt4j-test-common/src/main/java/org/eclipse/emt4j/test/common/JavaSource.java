package org.eclipse.emt4j.test.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JavaSource {
    private String javaSource;
    private String packageName;
    private String className;
    private String name;
    private String declare;
    private List<String> imports = new ArrayList<>();
    private List<FieldDesc> fields = new ArrayList<>();
    private List<MethodDesc> methods = new ArrayList<>();
    private boolean rawMode;

    public JavaSource(String className, String declare, String[] importClasses, FieldDesc[] fields, MethodDesc[] methods) {
        setName(className);
        this.declare = declare;
        if (importClasses != null) {
            this.imports = toList(importClasses);
        }
        if (fields != null) {
            this.fields = toList(fields);
        }
        if (methods != null) {
            this.methods = toList(methods);
        }
    }

    private <T> List<T> toList(T[] array) {
        if (array == null) {
            return null;
        } else {
            List<T> list = new ArrayList<>(array.length);
            for (T a : array) {
                list.add(a);
            }
            return list;
        }
    }

    public JavaSource(String className, String javaSource) {
        this.rawMode = true;
        setName(className);
        this.javaSource = javaSource;
    }

    public JavaSource(String className, String declare, List<String> importClasses, List<FieldDesc> fields, List<MethodDesc> methods) {
        setName(className);
        this.declare = declare;
        this.imports = importClasses;
        this.fields = fields;
        this.methods = methods;
    }

    private void setName(String className) {
        this.className = className;
        int i = className.lastIndexOf('.');
        if (i != -1) {
            this.packageName = className.substring(0, i);
            this.name = className.substring(i + 1);
        } else {
            this.name = className;
        }
    }


    public String getPackageName() {
        return packageName;
    }

    public String getClassName() {
        return className;
    }

    public String internalName() {
        return className.replace('.', '/');
    }

    public String getName() {
        return name;
    }

    public String buildJavaSourceCode() {
        if (rawMode) {
            return javaSource;
        } else {
            StringBuilder sb = new StringBuilder(512);
            if (packageName != null) {
                sb.append("package ").append(packageName).append(";").append('\n');
            }
            if (imports != null) {
                for (String importStr : imports) {
                    sb.append("import ").append(importStr).append(";").append('\n');
                }
            }
            sb.append(declare).append(" {").append('\n');
            if (fields != null) {
                for (FieldDesc field : fields) {
                    sb.append('\t').append(field.statement).append(";").append('\n');
                }
            }
            if (methods != null) {
                for (MethodDesc method : methods) {
                    sb.append('\t').append(method.statement).append('\n');
                }
            }

            sb.append('}').append('\n');
            return sb.toString();
        }
    }

    public static class FieldDesc {
        private String id;
        private String statement;

        public String getId() {
            return id;
        }

        public FieldDesc(String id, String statement) {
            this.id = id;
            this.statement = statement;
        }
    }

    public List<String> getImports() {
        return Collections.unmodifiableList(imports);
    }

    public List<FieldDesc> getFields() {
        return Collections.unmodifiableList(fields);
    }

    public List<MethodDesc> getMethods() {
        return Collections.unmodifiableList(methods);
    }

    public String getDeclare() {
        return declare;
    }

    public static class MethodDesc {
        private String id;
        private String statement;

        public MethodDesc(String id, String statement) {
            this.id = id;
            this.statement = statement;
        }

        public String getId() {
            return id;
        }
    }


    public static final String FATJAR_TRAMPOLINE_CLASSNAME = "LaunchTrampoline";
    public static final JavaSource[] FATJAR_TRAMPOLINE_SOURCE = new JavaSource[]{
            new JavaSource(FATJAR_TRAMPOLINE_CLASSNAME, "public class LaunchTrampoline", null, null, new MethodDesc[]{
                    new MethodDesc("main",
                            "public static void main(String[] args) throws Exception {\n" +
                                    "        Class.forName(\"JarLauncher\").getDeclaredMethod(\"run\", Class.class, String[].class).invoke(null, LaunchTrampoline.class, args);\n" +
                                    "    }")
            })
    };
}
