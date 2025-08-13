package org.eclipse.emt4j.common.staticanalysis.impl;

import org.junit.Test;
import soot.G;
import soot.options.Options;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.SootField;
import soot.jimple.Jimple;
import soot.jimple.StaticFieldRef;

import java.io.File;

import static org.junit.Assert.*;

public class BaseAnalyzerTest {

    /** Init Soot for JDK 8 (rt.jar on classpath). */
    private void initSoot() {
        G.reset();

        String sep = File.pathSeparator;

        // JUnit/IDE CP + module build outputs (adjust if your build dir differs)
        String cp = String.join(sep,
                Scene.v().defaultClassPath(),
                "target/classes",
                "target/test-classes"
        );

        Options.v().set_prepend_classpath(true);
        Options.v().set_soot_classpath(cp);
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_no_bodies_for_excluded(true);
        Options.v().set_whole_program(true);
        Options.v().set_output_format(Options.output_format_none);

        // Keep locals from being optimized away (not strictly required for this test anymore)
        Options.v().setPhaseOption("jb", "use-original-names:true");
        Options.v().setPhaseOption("jb.cp",  "enabled:false");
        Options.v().setPhaseOption("jb.dae", "enabled:false");
        Options.v().setPhaseOption("jb.uce", "enabled:false");
        Options.v().setPhaseOption("jb.lns", "enabled:false");

        Scene.v().loadBasicClasses();
        Scene.v().loadNecessaryClasses();
    }

    /** Load our helper class and return both the fieldRef and its <clinit> method. */
    private static class Fixture {
        final StaticFieldRef fieldRef;
        final SootMethod clinit;
        Fixture(StaticFieldRef fr, SootMethod m) { this.fieldRef = fr; this.clinit = m; }
    }

    private Fixture loadFixture() {
        initSoot();

        final String fqcn = "org.eclipse.emt4j.common.staticanalysis.impl.TestClassWithStaticField";

        // Bring class up so we can safely query members
        SootClass sc = Scene.v().forceResolve(fqcn, SootClass.SIGNATURES);
        sc.setApplicationClass();
        Scene.v().loadNecessaryClasses();

        // field ref
        SootField sf = sc.getFieldByName("myField");
        StaticFieldRef fieldRef = Jimple.v().newStaticFieldRef(sf.makeRef());

        // find <clinit>
        SootMethod clinit = sc.getMethodByName("<clinit>");
        // ensure body can be retrieved when the tested method runs
        // (we don't retrieve it here; the target methods will)
        return new Fixture(fieldRef, clinit);
    }

    @Test
    public void testWithRelease_releasesActiveBody() {
        Fixture fx = loadFixture();

        // Sanity: before calling, there should be no active body
        assertFalse("Precondition: <clinit> should not have active body",
                fx.clinit.hasActiveBody());

        // Call method that DOES release
        BaseAnalyzer.globalTarget(fx.fieldRef);

        // After call: the body should be released
        assertFalse("Expected <clinit> active body to be released by globalTarget(..)",
                fx.clinit.hasActiveBody());
    }

    @Test
    public void testWithoutRelease_keepsActiveBody() {
        Fixture fx = loadFixture();

        // Sanity: before calling, there should be no active body
        assertFalse("Precondition: <clinit> should not have active body",
                fx.clinit.hasActiveBody());

        // Call method that does NOT release
        BaseAnalyzer.globalTargetNoRelease(fx.fieldRef);

        // After call: the body should still be active
        assertTrue("Expected <clinit> to still have active body when not released",
                fx.clinit.hasActiveBody());

        // Cleanup so this test doesn't pollute the next one
        fx.clinit.releaseActiveBody();
        assertFalse("Cleanup: active body should be released", fx.clinit.hasActiveBody());
    }
}
