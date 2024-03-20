package org.eclipse.emt4j.test;

// 1 unfixed problem
public class RunFinalizerOnExit implements Problem{
    public void produce() {
        Runtime.runFinalizersOnExit(false);
        System.runFinalizersOnExit(false);
    }
}
