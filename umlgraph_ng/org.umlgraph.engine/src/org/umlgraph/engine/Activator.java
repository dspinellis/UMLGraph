/*
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

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * The bundle activator. Only used when running in OSGi.
 */
public class Activator implements BundleActivator {

    private BundleContext context;
    private static Activator instance;

    public Activator() {
        instance = this;
    }

    public void start(BundleContext context) throws Exception {
        this.context = context;
    }

    public void stop(BundleContext context) throws Exception {
        this.context = null;
    }

    /**
     * Returns whether we are running as an OSGi bundle.
     * 
     * @return whether we are running as an OSGi bundle
     */
    static boolean isRunningOSGi() {
        return instance != null && instance.context != null;
    }
}
