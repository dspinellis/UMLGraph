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

/**
 * A tag interface for classes defining settings.
 * The contract for SettingDefinitions implementors is:
 * <ul>
 * <li>Classes implementing this interface must be public</li>
 * <li>Each option must be declared as a public static field of a non-primitive type</li>
 * <li>Default values can be defined using Java's field initialization, 
 * or by value assignment</li>
 * <li>Names of fields
 * are expected to be unique across all different definition classes used
 * in the same Setting node hierarchy.</li>
 * </ul>
 */
public interface SettingDefinitions {

}
