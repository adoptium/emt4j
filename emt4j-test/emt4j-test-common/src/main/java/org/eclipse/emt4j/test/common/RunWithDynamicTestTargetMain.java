package org.eclipse.emt4j.test.common;

import java.lang.reflect.Method;

/**
 * When test target is dynamic,should call this class' main.
 */
public class RunWithDynamicTestTargetMain {

    /**
     * args[0]: The test class that generate the dynamic jar or classes
     * args[1]: The work dir that save the dynamic jar or classes
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        Class checkClass = Class.forName(args[0]);
        String workDir = args[1];
        Method method = checkClass.getDeclaredMethod("prepareDynamicTestTarget", String.class);
        method.invoke(checkClass.getDeclaredConstructor().newInstance(), workDir);
    }
}
