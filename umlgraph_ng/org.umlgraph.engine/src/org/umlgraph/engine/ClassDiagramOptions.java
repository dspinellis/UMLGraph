/*
 * (C) Copyright 2002-2008 Diomidis Spinellis
 * (C) Copyright 2008 Abstratt Technologies
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
package org.umlgraph.engine;

import org.umlgraph.settings.SettingDefinitions;

/**
 * Constants for class diagram options.
 */
public class ClassDiagramOptions implements SettingDefinitions {
    /**
     * Produce fully-qualified class names.
     */
    public static Boolean classDiagramQualifiedNames = false;

    /**
     * When using qualified class names, put the package name in the line after the class name, in order to reduce the width of class nodes.
     */
    public static Boolean classDiagramPostfixQualifiedNames = false;
    
    /**
     * Try to automatically infer dependencies between classes by inspecting operation and attribute types. See the class diagram inference chapter for more details. Disabled by default. 
     */
    public static Boolean classDiagramInferDependency = false;
    
    /**
     * Try to automatically infer association relationships between classes by inspecting attribute types. See the class diagram inference chapter for further details. Disabled by default.
     */
    public static Boolean classDiagramInferUsage = false;
    
    /**
     * The type of relationship inferred when -inferrel is activated. Defaults to "navassoc" (see the class modelling chapter for a list of relationship types).
     */
    public static String classDiagramInferAssociationType = "navassoc";

    /**
     * Specifies the lowest visibility level of elements used to infer dependencies among classes. Possible values are private, package, protected, public, in this order. The default value is private. Use higher levels to limit the number of inferred dependencies.  
     */
    public static String classDiagramInferDependencyVisibility = "private";  
}
