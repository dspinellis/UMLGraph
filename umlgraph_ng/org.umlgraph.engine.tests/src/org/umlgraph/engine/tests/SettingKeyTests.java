package org.umlgraph.engine.tests;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.umlgraph.settings.SettingKey;

public class SettingKeyTests extends TestCase {
    public SettingKeyTests(String name) {
	super(name);
    }

    public void testRoot() {
	SettingKey key = new SettingKey("/");
	assertNull(key.getOptionName());
	assertNotNull(key.getNodePath());
	assertEquals(0, key.getNodePath().length);
    }

    public void testRootOption() {
	SettingKey key = new SettingKey("@option1");
	assertEquals("option1", key.getOptionName());
	assertNotNull(key.getNodePath());
	assertEquals(0, key.getNodePath().length);
    }

    public void testFirstLevel() {
	SettingKey key = new SettingKey("context1");
	assertNull(key.getOptionName());
	assertEquals(1, key.getNodePath().length);
	assertEquals("context1", key.getNodePath()[0]);
    }

    public void testFirstLevelOption() {
	SettingKey key = new SettingKey("context1/@option1");
	assertEquals("option1", key.getOptionName());
	assertEquals(1, key.getNodePath().length);
	assertEquals("context1", key.getNodePath()[0]);
    }

    public void testSecondLevel() {
	SettingKey key = new SettingKey("context1/context2");
	assertNull(key.getOptionName());
	assertEquals(2, key.getNodePath().length);
	assertEquals("context1", key.getNodePath()[0]);
	assertEquals("context2", key.getNodePath()[1]);
    }

    public void testSecondLevelOption() {
	SettingKey key = new SettingKey("context1/context2/@option1");
	assertEquals("option1", key.getOptionName());
	assertEquals(2, key.getNodePath().length);
	assertEquals("context1", key.getNodePath()[0]);
	assertEquals("context2", key.getNodePath()[1]);
    }
    
    public void testVariations() {
	assertEquals(new SettingKey(""),new SettingKey("/"));
	assertEquals(new SettingKey("context1"),new SettingKey("/context1"));
	assertEquals(new SettingKey("context1@option1"),new SettingKey("/context1/@option1"));
	assertEquals(new SettingKey("context1@option1"),new SettingKey("context1/@option1"));
	assertEquals(new SettingKey("context1@option1"),new SettingKey("/context1@option1"));
	assertEquals(new SettingKey("context1@option1"),new SettingKey("/context1/@option1"));	
	assertFalse(new SettingKey("context1").equals(new SettingKey("context2")));
    }

    public static Test suite() {
	return new TestSuite(SettingKeyTests.class);
    }

}
