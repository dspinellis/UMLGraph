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
 * <li> options are typed, so an attempt at setting them with the wrong value
 * type causes an exception </li>
 * <li> option contexts are statically typed, meaning that set of available
 * options for a context is defined at compile time and trying to set an
 * unexpected option value causes an exception. </li>
 * </ul>
 * Non-features:
 * <ul>
 * <li> persistence </li>
 * </ul>
 */
public class Settings {

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

	private Settings(Settings parent) {
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
		if (!acceptDefault) {
			Settings node = existingNode(key.getNodePath());
			return node == null ? null : (T) node.values.get(key.getKey());
		}
		Settings node = lastExistingNode(key.getNodePath(), 0);
		return node.searchValue(key.getKey());
	}

	private <T> T searchValue(String key) {
		if (!isDefined(key))
			throw new IllegalArgumentException("unknown setting: " + key);
		T ownValue = (T) values.get(key);
		if (ownValue != null)
			return ownValue;
		return (T) (parent == null ? getDefaultValue(key) : parent
				.searchValue(key));
	}

	private boolean isDefined(String key) {
		return findField(key) != null;
	}

	private <T> T getDefaultValue(String key) {
		Field field = findField(key);
		if (field == null)
			throw new IllegalArgumentException(
					"unknown setting or undefined default: " + key);
		try {
			return (T) field.get(null);
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException("unknown setting: " + key);
		}
	}

	private Field findField(String key) {
		if (parent != null)
			return parent.findField(key);
		for (Class<? extends SettingDefinitions> definitionClass : definitions)
			try {
				Field field = definitionClass.getDeclaredField(key);
				if (!field.getType().isPrimitive()
						&& Modifier.isStatic(field.getModifiers()))
					return field;
			} catch (NoSuchFieldException e) {
				// keep looking...
			}
		return null;
	}

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

	public <T> T get(String path) {
		return get(new SettingKey(path), true);
	}

	/**
	 * Sets the value for the given option.
	 * 
	 * @param key
	 *            the option key
	 * @param value
	 *            the new value, or <code>null</code>
	 */
	public <T> void set(SettingKey key, T value) {
		if (!isDefined(key.getKey()))
			throw new IllegalArgumentException("unknown setting: " + key);
		if (value == null) {
			Settings node = existingNode(key.getNodePath());
			if (node != null)
				node.values.remove(key.getKey());
		} else {
			Field field = findField(key.getKey());
			if (!field.getType().isAssignableFrom(value.getClass()))
				throw new ClassCastException(value.getClass().getName());
			Settings node = createNode(key.getNodePath(), 0);
			node.values.put(key.getKey(), value);
		}
	}

	public <T> void set(String path, T value) {
		set(new SettingKey(path), value);
	}

	public <T> void setToDefault(String path) {
		setToDefault(new SettingKey(path));
	}

	/**
	 * Sets the given option to its current default value.
	 * 
	 * @param settingKey
	 *            the key to set to default
	 */
	public <T> void setToDefault(SettingKey key) {
		Object defaultValue = getDefaultValue(key.getKey());
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
