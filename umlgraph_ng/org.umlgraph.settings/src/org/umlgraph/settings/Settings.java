/*
 * (C) Copyright 2007-2008 Abstratt Technologies
 *
 * Permission to use, copy, and distribute this software and its
 * documentation for any purpose and without fee is hereby granted,
 * provided that the above copyright notice appear in all copies and that
 * both that copyright notice and this permission notice appear in
 * supporting documentation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND WITHOUT ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTIES OF
 * MERCHANTIBILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 *
 * $Id$
 *
 */
package org.umlgraph.settings;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * A typed hierarchic preference mechanism that takes advantage of generics for
 * providing a simpler protocol.
 * 
 * Features:
 * <ul>
 * <li>default values</li>
 * <li>contexts are hierarchic, option value lookup falls back to parent if
 * option is not found at the current level</li>
 * <li> the set of available options is defined at compile time and trying to
 * set an unknown option value causes an exception. </li>
 * <li> options are typed, so an attempt at setting them with the wrong value
 * type causes an exception </li>
 * </ul>
 * Non-features:
 * <ul>
 * <li> persistence </li>
 * </ul>
 */
public class Settings {

	/*
	 * The setting definitions for this setting node hierarchy (only set for the
	 * root node). The definitions only define the available options and the
	 * expected types.
	 */
	private Class<? extends SettingDefinitions>[] definitions;

	/*
	 * The child setting nodes.
	 */
	private Map<String, Settings> children = new HashMap<String, Settings>();

	/*
	 * The setting values defined for this setting node.
	 */
	private Map<String, Object> values = new HashMap<String, Object>();

	private Settings parent;

	/**
	 * Constructs a root settings object.
	 * 
	 * @param definitions
	 *            the setting definition classes
	 */
	public Settings(Class<? extends SettingDefinitions>... definitions) {
		if (definitions.length == 0)
			throw new IllegalArgumentException("no definitions provided");
		this.definitions = definitions;
	}

	/**
	 * Constructs a child setting node.
	 * 
	 * @param parent
	 *            the parent node, never <code>null</code>
	 */
	private Settings(Settings parent) {
		assert parent != null;
		this.parent = parent;
	}

	/**
	 * Returns the setting node corresponding to the given path. If the node
	 * does not exist, returns a new empty node.
	 * 
	 * @param path
	 *            the setting node path
	 * @return the setting node corresponding to the given path
	 */
	public Settings node(String[] path) {
		return createNode(path, 0);
	}

	/**
	 * Returns the setting node corresponding to the given path. If the node
	 * does not exist, returns a new empty node.
	 * 
	 * @param path
	 *            the setting node path
	 * @return the setting node corresponding to the given path
	 */
	public Settings node(String path) {
		return node(new SettingKey(path).getNodePath());
	}

	/**
	 * Creates a setting node under the given path, or returns the existing one.
	 */
	private Settings createNode(String[] path, int start) {
		if (path.length == start)
			return this;
		Settings found = children.get(path[start]);
		if (found == null)
			children.put(path[start], found = new Settings(this));
		if (start == path.length - 1)
			return found;
		return found.createNode(path, start + 1);
	}

	/**
	 * Returns whether the given node exists.
	 */
	public boolean nodeExists(String[] path) {
		return existingNode(path) != null;
	}

	/**
	 * Returns whether the given node exists.
	 */
	public boolean nodeExists(String path) {
		return nodeExists(new SettingKey(path).getNodePath());
	}

	/**
	 * Returns an existing setting node corresponding to the given path.
	 * 
	 * @param path
	 *            the setting path
	 * @return the settings object, or <code>null</code>
	 */
	public Settings existingNode(String[] segments) {
		Settings current = this;
		for (String segment : segments) {
			current = current.children.get(segment);
			if (current == null)
				return null;
		}
		return current;
	}

	/**
	 * Returns an existing node corresponding to the given path, or
	 * <code>null</code> if none exists.
	 * 
	 * @param path
	 *            the path of the node to look for
	 * @return the node found, or <code>null</code>
	 */
	public Settings existingNode(String path) {
		return existingNode(new SettingKey(path).getNodePath());
	}

	/**
	 * Returns the value of the option corresponding to the given key. If
	 * acceptDefault is <code>true</code> and a value for the option does not
	 * exist, returns the default value. Otherwise, returns <code>null</code>.
	 * 
	 * @param key
	 *            the option key
	 * @param acceptDefault
	 *            whether a default value should be returned
	 * @return the value for the given key, or <code>null</code>
	 */
	public <T> T get(SettingKey key, boolean acceptDefault) {
		if (!isDefined(key.getOptionName()))
			throw new IllegalArgumentException("unknown setting: "
					+ key.getOptionName());
		if (!acceptDefault) {
			Settings node = existingNode(key.getNodePath());
			return node == null ? null : (T) node.values.get(key
					.getOptionName());
		}
		Settings node = lastExistingNode(key.getNodePath(), 0);
		return node.searchValue(key.getOptionName());
	}

	/**
	 * Returns the value for the option with the given name. Falls back to the
	 * default value, if one is defined.
	 * 
	 * @param optionName
	 *            the option to search a value for
	 * @return the value for the given option, or <code>null</code>
	 */
	private <T> T searchValue(String optionName) {
		T ownValue = (T) values.get(optionName);
		if (ownValue != null)
			return ownValue;
		return (T) (parent == null ? getDefaultValue(optionName) : parent
				.searchValue(optionName));
	}

	/**
	 * Returns whether the given option name is defined for this setting node
	 * hierarchy.
	 * 
	 * @param optionName
	 *            the option name
	 * @return <code>true</code> if an option with the given name is defined,
	 *         <code>false</code> otherwise
	 */
	private boolean isDefined(String optionName) {
		return findOptionField(optionName) != null;
	}

	/**
	 * Returns the default value for the given option, or <code>null</code> if
	 * a default value is not defined.
	 * 
	 * @param optionName
	 * @return the default value, or <code>null</code>
	 */
	private <T> T getDefaultValue(String optionName) {
		Field field = findOptionField(optionName);
		if (field == null)
			throw new IllegalArgumentException(
					"unknown setting or undefined default: " + optionName);
		try {
			return (T) field.get(null);
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException("unknown setting: " + optionName);
		}
	}

	/**
	 * Finds the field corresponding to the given option name.
	 * 
	 * @param optionName
	 * @return
	 */
	private Field findOptionField(String optionName) {
		if (parent != null)
			return parent.findOptionField(optionName);
		for (Class<? extends SettingDefinitions> definitionClass : definitions)
			try {
				Field field = definitionClass.getDeclaredField(optionName);
				if (!field.getType().isPrimitive()
						&& Modifier.isStatic(field.getModifiers()))
					return field;
			} catch (NoSuchFieldException e) {
				// keep looking...
			}
		return null;
	}

	/**
	 * Returns the last existing node for the given node path.
	 * 
	 * @param nodePath
	 *            node path
	 * @param start
	 *            starting level
	 * @return the last existing node
	 */
	private Settings lastExistingNode(String[] nodePath, int start) {
		if (start == nodePath.length)
			return this;
		Settings immediateChild = children.get(nodePath[start]);
		return immediateChild == null ? this : immediateChild.lastExistingNode(
				nodePath, start + 1);
	}

	/**
	 * Returns the value of the option corresponding to the given path. If
	 * acceptDefault is <code>true</code> and a value for the option does not
	 * exist, returns the default value. Otherwise, returns <code>null</code>.
	 * This is just a convenience method for {@link #get(SettingKey, boolean)}.
	 * 
	 * @param path
	 *            the option path
	 * @param acceptDefault
	 *            whether a default value should be returned
	 * @return the value for the given path, or <code>null</code>
	 */

	public <T> T get(String path, boolean acceptDefault) {
		return get(new SettingKey(path), acceptDefault);
	}

	/**
	 * Returns the value of the option corresponding to the given path (falls
	 * back to a default value, if one is defined). This is just a convenience
	 * method for {@link #get(SettingKey, boolean)}.
	 * 
	 * @param path
	 *            the option path
	 * @return the value for the given path, or <code>null</code>
	 */
	public <T> T get(String path) {
		return get(new SettingKey(path), true);
	}

	/**
	 * Sets the value for the given option (or clears it).
	 * 
	 * @param key
	 *            the option key
	 * @param value
	 *            the new value, or <code>null</code>
	 */
	public <T> void set(SettingKey key, T value) {
		if (!isDefined(key.getOptionName()))
			throw new IllegalArgumentException("unknown setting: " + key);
		if (value == null) {
			Settings node = existingNode(key.getNodePath());
			if (node != null)
				node.values.remove(key.getOptionName());
		} else {
			Field field = findOptionField(key.getOptionName());
			if (!field.getType().isAssignableFrom(value.getClass()))
				throw new ClassCastException(value.getClass().getName());
			Settings node = createNode(key.getNodePath(), 0);
			node.values.put(key.getOptionName(), value);
		}
	}

	/**
	 * Sets the value of the given option (or clears it).
	 * 
	 * @param path
	 *            the option path
	 * @param value
	 *            the new value, or <code>null</code>
	 */
	public <T> void set(String path, T value) {
		set(new SettingKey(path), value);
	}

	/**
	 * Sets the given option to the default value.
	 * 
	 * @param path
	 *            the option path
	 */
	public <T> void setToDefault(String path) {
		setToDefault(new SettingKey(path));
	}

	/**
	 * Sets the given option to the default value.
	 * 
	 * @param settingKey
	 *            the key to set to default
	 */
	public <T> void setToDefault(SettingKey key) {
		Object defaultValue = getDefaultValue(key.getOptionName());
		if (defaultValue == null)
			throw new IllegalStateException("no default value for " + key);
		set(key, defaultValue);
	}

	/**
	 * Returns the parent settings, or <code>null</code> if this is a root
	 * setting.
	 * 
	 * @return the parent settings, or <code>null</code>
	 */
	public Settings getParent() {
		return parent;
	}
}
