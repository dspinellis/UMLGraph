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
