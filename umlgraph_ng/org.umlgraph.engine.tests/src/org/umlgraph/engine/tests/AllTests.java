package org.umlgraph.engine.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {
    public static Test suite() {
	TestSuite allTests = new TestSuite(AllTests.class.getName());
	allTests.addTest(SettingKeyTests.suite());
	allTests.addTest(SettingTests.suite());
	return allTests;
    }
}
