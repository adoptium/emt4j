package org.eclipse.emt4j.test;

// 1 fixed problem
public class SM implements Problem {
    @Override
    public void produce() {
        SecurityManager securityManager = new SecurityManager();
        securityManager.checkSystemClipboardAccess();
    }
}
