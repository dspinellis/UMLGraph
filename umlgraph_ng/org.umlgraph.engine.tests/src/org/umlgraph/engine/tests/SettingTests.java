package org.umlgraph.engine.tests;

import java.util.Collection;

import org.umlgraph.engine.options.Settings;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class SettingTests extends TestCase {
    public void testBasic() {
	Settings root = new Settings();
	root.set("@option2", "foo");
	root.set("@option3", Boolean.TRUE);

	assertNull(root.get("@option1"));
	assertEquals("foo", root.get("@option2"));
	assertTrue(root.<Boolean> get("@option3"));
    }

    public void testDefault() {
	Settings root = new Settings();
	root.set("@option1", "foo");
	root.setDefault("@option2", Boolean.TRUE);
	root.set("@option3", 42);
	root.setDefault("@option3", 52);

	assertEquals("foo", root.get("@option1"));
	assertEquals(Boolean.TRUE, root.get("@option2"));
	assertEquals(42, root.get("@option3"));
    }

    public void testRemoval() {
	Settings root = new Settings();
	root.set("@option1", "foo");
	root.setDefault("@option1", "bar");
	assertEquals("foo", root.get("@option1"));

	root.set("@option1", null);
	assertEquals("bar", root.get("@option1"));

	root.setDefault("@option1", null);
	assertNull(root.get("@option1"));

    }

    public void testDefaultFirstLevel() {
	Settings root = new Settings();
	root.set("context1@option1", "foo");
	root.setDefault("context1@option2", Boolean.TRUE);
	root.set("context1@option3", 42);
	root.setDefault("context1@option3", 52);

	assertEquals("foo", root.get("context1@option1"));
	assertEquals(Boolean.TRUE, root.get("context1@option2"));
	assertEquals(42, root.get("context1@option3"));
    }

    public void testDefaultSecondLevel() {
	Settings root = new Settings();
	root.set("context1/context2@option1", "foo");
	root.setDefault("context1/context2@option2", Boolean.TRUE);
	root.set("context1/context2@option3", 42);
	root.setDefault("context1/context2@option3", 52);

	assertEquals("foo", root.get("context1/context2@option1"));
	assertEquals(Boolean.TRUE, root.get("context1/context2@option2"));
	assertEquals(42, root.get("context1/context2@option3"));
    }

    public void testFirstLevel() {
	Settings root = new Settings();
	root.set("context1@option2", "foo");
	root.set("context1@option3", Boolean.TRUE);

	assertNull(root.get("context1@option1"));
	assertEquals("foo", root.get("context1@option2"));
	assertTrue(root.<Boolean> get("context1@option3"));
    }

    public void testSecondLevel() {
	Settings root = new Settings();
	root.set("context1/context2/@option2", "foo");
	root.set("context1/context2/@option3", Boolean.TRUE);

	assertNull(root.get("context1/context2/@option1"));
	assertEquals("foo", root.get("context1/context2/@option2"));
	assertTrue(root.<Boolean> get("context1/context2/@option3"));
    }

    public void testBasicFallback() {
	Settings root = new Settings();
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

    public void testFallbackWithDefaults() {
	Settings root = new Settings();
	root.set("@option1", "foo");
	root.setDefault("@option2", Boolean.TRUE);

	root.setDefault("context1/@option1", "zoo");

	root.setDefault("context1/context2/@option1", null);

	assertEquals("zoo", root.get("context1@option1"));
	assertEquals(Boolean.TRUE, root.get("context1@option2"));
	assertNull(root.get("context1@option1", false));

	assertEquals("zoo", root.get("context1/context2@option1"));
	assertEquals(Boolean.TRUE, root.get("context1/context@option2"));
    }

    public void testSetToDefault() {
	Settings root = new Settings();
	root.set("@option1", "foo");
	root.setDefault("@option1", "bar");

	root.set("context1/@option1", "zoo");
	root.set("context1/@option2", Boolean.TRUE);

	assertEquals("zoo", root.get("context1/context2@option1"));
	root.setToDefault("context1/context2@option1");
	assertEquals("bar", root.get("context1/context2@option1"));

	assertEquals(true, root.get("context1/context2@option2"));
	try {
	    root.setToDefault("context1/context2@option2");
	    fail("should have failed");
	} catch (IllegalStateException ise) {
	    // expected
	}
    }

    public void testDefaultCancellation() {
	Settings root = new Settings();
	root.set("@option2", "foo");
	root.setDefault("@option3", Boolean.TRUE);
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

    public void testOptionKeys() {
	Settings root = new Settings();
	root.set("@option2", "foo");
	root.set("@option3", Boolean.TRUE);
	root.setDefault("@option4", 42);

	Collection<String> keys = root.getOptionKeys();
	assertEquals(2, keys.size());
	assertFalse(keys.contains("option1"));
	assertTrue(keys.contains("option2"));
	assertTrue(keys.contains("option3"));
	assertFalse(keys.contains("option4"));
    }

    public void testContextCreation() {
	Settings root = new Settings();
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

    public void testParenting() {
	Settings root = new Settings();
	Settings context1 = root.node("context1");
	Settings context2 = root.node("context1/context2");
	Settings context3 = root.node("context1/context3");

	assertNull(root.getParent());
	assertSame(root, context1.getParent());
	assertSame(context1, context2.getParent());
	assertSame(context1, context3.getParent());
    }

    public void testContextCreationByOption() {
	Settings root = new Settings();
	root.set("@option1", "foo");
	root.set("context1@option2", "bar");
	root.set("context1/context2@option3", "zoo");

	assertTrue(root.nodeExists(""));
	assertTrue(root.nodeExists("/"));
	assertTrue(root.nodeExists("context1"));
	assertTrue(root.nodeExists("context1/context2"));
	assertFalse(root.nodeExists("context2"));
    }

    public SettingTests(String name) {
	super(name);
    }

    public static Test suite() {
	return new TestSuite(SettingTests.class);
    }
}
