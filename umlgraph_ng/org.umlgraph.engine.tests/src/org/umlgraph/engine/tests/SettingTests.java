package org.umlgraph.engine.tests;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.umlgraph.settings.SettingDefinitions;
import org.umlgraph.settings.Settings;

public class SettingTests extends TestCase {
    public static class TestBasicDefinitions implements SettingDefinitions {
	public static String option1;
	public static String option2;
	public static Boolean option3;
    }

    public static class TestDefault implements SettingDefinitions {
	public static String option1 = "bar";
	public static Boolean option2 = true;
	public static Integer option3 = 52;
    }

    public static class TestFallbackDefinitions implements SettingDefinitions {
	public static String option1;
	public static String option2;
	public static Boolean option3;
	public static Integer option4;
    }

    public static class TestRemovalDefinitions implements SettingDefinitions {
	public static String option1 = "bar";
	public static String option2;
    }

    public static Test suite() {
	return new TestSuite(SettingTests.class);
    }

    public SettingTests(String name) {
	super(name);
    }
    
    public void testBasicFallback() {
	Settings root = new Settings(TestFallbackDefinitions.class);
	root.set("@option2", "foo");
	root.set("@option3", Boolean.TRUE);
	root.set("context1/@option3", Boolean.FALSE);
	root.set("context1/@option4", 42);
	root.set("context1/context2/@option1", "zoo");
	root.set("context1/context2/@option2", "bar");
	root.set("context1/context2/@option3", Boolean.TRUE);

	assertNull(root.get("@option1"));
	assertNull(root.get("context1/@option1"));
	assertEquals("zoo", root.get("context1/context2/@option1"));

	assertEquals("foo", root.get("@option2"));
	assertEquals("foo", root.get("context1/@option2"));
	assertEquals("bar", root.get("context1/context2@option2"));

	assertEquals(Boolean.TRUE, root.get("@option3"));
	assertEquals(Boolean.FALSE, root.get("context1/@option3"));
	assertEquals(Boolean.TRUE, root.get("context1/context2@option3"));

	assertEquals(null, root.get("@option4"));
	assertEquals(42, root.get("context1/@option4"));
	assertEquals(42, root.get("context1/context2@option4"));
    }

    public void testContextCreation() {
	Settings root = new Settings(TestBasicDefinitions.class);
	Settings context1 = root.node("context1");
	assertNotNull(context1);
	Settings context2 = root.node("context1/context2");
	assertNotNull(context2);
	Settings context3 = root.node("context1/context3");
	assertNotNull(context3);

	assertTrue(root.nodeExists(""));
	assertTrue(root.nodeExists("/"));
	assertTrue(root.nodeExists("context1"));
	assertTrue(root.nodeExists("context1/context2"));
	assertFalse(root.nodeExists("context2"));
	assertTrue(root.nodeExists("context1/context3"));
    }

    public void testContextCreationByOption() {
	Settings root = new Settings(TestBasicDefinitions.class);
	root.set("@option1", "foo");
	root.set("context1@option2", "bar");
	root.set("context1/context2@option3", "zoo");

	assertTrue(root.nodeExists(""));
	assertTrue(root.nodeExists("/"));
	assertTrue(root.nodeExists("context1"));
	assertTrue(root.nodeExists("context1/context2"));
	assertFalse(root.nodeExists("context2"));
    }

    public void testDefault() {
	Settings root = new Settings(TestDefault.class);
	root.set("@option1", "foo");
	root.set("@option3", 42);

	assertEquals("foo", root.get("@option1"));
	assertEquals(Boolean.TRUE, root.get("@option2"));
	assertEquals(42, root.get("@option3"));
	root.set("@option3", null);
	assertEquals(52, root.get("@option3"));
    }

    public void testDefaultCancellation() {
	Settings root = new Settings(TestBasicDefinitions.class);
	root.set("@option2", "foo");
	root.set("context1/@option3", Boolean.FALSE);
	root.set("context1/@option4", 42);
	root.set("context1/context2/@option1", "zoo");
	root.set("context1/context2/@option2", "bar");
	root.set("context1/context2/@option3", Boolean.TRUE);

	assertNull(root.get("@option1"));
	assertNull(root.get("context1/@option1"));
	assertEquals("zoo", root.get("context1/context2/@option1"));

	assertEquals("foo", root.get("@option2"));
	assertEquals("foo", root.get("context1/@option2"));
	assertEquals("bar", root.get("context1/context2@option2"));

	assertEquals(null, root.get("@option3"));
	assertEquals(Boolean.FALSE, root.get("context1/@option3"));
	assertEquals(Boolean.TRUE, root.get("context1/context2@option3"));
    }

    public void testDefaultFirstLevel() {
	Settings root = new Settings(TestDefault.class);
	root.set("context1@option1", "foo");
	root.set("context1@option3", 42);

	assertEquals("foo", root.get("context1@option1"));
	assertEquals(Boolean.TRUE, root.get("context1@option2"));
	assertEquals(42, root.get("context1@option3"));
    }

    public void testDefaultSecondLevel() {
	Settings root = new Settings(TestDefault.class);
	root.set("context1/context2@option1", "foo");
	root.set("context1/context2@option3", 42);

	assertEquals("foo", root.get("context1/context2@option1"));
	assertEquals(Boolean.TRUE, root.get("context1/context2@option2"));
	assertEquals(42, root.get("context1/context2@option3"));
    }

    public void testFallbackWithDefaults() {
	Settings root = new Settings(TestDefault.class);
	root.set("@option1", "zoo");
	
	assertEquals("zoo", root.get("context1@option1"));
	assertEquals(Boolean.TRUE, root.get("context1@option2"));
	assertNull(root.get("context1@option1", false));

	root.set("@option1", null);
	assertEquals("bar", root.get("context1/context2@option1"));
	assertEquals(Boolean.TRUE, root.get("context1/context@option2"));
    }

    public void testFirstLevel() {
	Settings root = new Settings(TestBasicDefinitions.class);
	root.set("context1@option2", "foo");
	root.set("context1@option3", Boolean.TRUE);

	assertNull(root.get("context1@option1"));
	assertEquals("foo", root.get("context1@option2"));
	assertTrue(root.<Boolean> get("context1@option3"));
    }

    public void testParenting() {
	Settings root = new Settings(TestBasicDefinitions.class);
	Settings context1 = root.node("context1");
	Settings context2 = root.node("context1/context2");
	Settings context3 = root.node("context1/context3");

	assertNull(root.getParent());
	assertSame(root, context1.getParent());
	assertSame(context1, context2.getParent());
	assertSame(context1, context3.getParent());
    }

    public void testRemoval() {
	Settings root = new Settings(TestRemovalDefinitions.class);
	root.set("@option1", "foo");
	root.set("@option2", "zoo");
	assertEquals("foo", root.get("@option1"));
	assertEquals("zoo", root.get("@option2"));

	root.set("@option1", null);
	root.set("@option2", null);
	assertEquals("bar", root.get("@option1"));
	assertNull(root.get("@option2"));
    }

    public void testRoot() {
	Settings root = new Settings(TestBasicDefinitions.class);
	root.set("@option2", "foo");
	root.set("@option3", Boolean.TRUE);

	assertNull(root.get("@option1"));
	assertEquals("foo", root.get("@option2"));
	assertTrue(root.<Boolean> get("@option3"));
    }

    public void testSecondLevel() {
	Settings root = new Settings(TestBasicDefinitions.class);
	root.set("context1/context2/@option2", "foo");
	root.set("context1/context2/@option3", Boolean.TRUE);

	assertNull(root.get("context1/context2/@option1"));
	assertEquals("foo", root.get("context1/context2/@option2"));
	assertTrue(root.<Boolean> get("context1/context2/@option3"));
    }

    public void testSetToDefault() {
	Settings root = new Settings(TestDefault.class);
	root.set("@option1", "foo");

	root.set("context1/@option1", "zoo");
	root.set("context1/@option2", Boolean.TRUE);

	assertEquals("zoo", root.get("context1/context2@option1"));
	root.setToDefault("context1/context2@option1");
	assertEquals("bar", root.get("context1/context2@option1"));

	assertEquals(true, root.get("context1/context2@option2"));
	// clear default value
	TestDefault.option2 = null;
	try {
	    root.setToDefault("context1/context2@option2");
	    fail("should have failed");
	} catch (IllegalStateException ise) {
	    // expected
	}
    }
}
