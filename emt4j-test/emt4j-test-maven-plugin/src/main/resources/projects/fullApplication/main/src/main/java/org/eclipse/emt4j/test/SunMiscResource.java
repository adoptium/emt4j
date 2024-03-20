package org.eclipse.emt4j.test;

// 1 fixed problem using compatability package
public class SunMiscResource implements Problem {
    @Override
    public void produce() {
        System.out.println(sun.misc.Resource.class.getName());
    }
}
