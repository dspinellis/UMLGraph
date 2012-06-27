package org.umlgraph.doclet;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

class Version {
    private final static String filename = "version.properties";
    private final static String key = "umlgraph.version";
    public static String VERSION;

    static {
        Properties props = new Properties();
        InputStream in = Version.class.getClassLoader().getResourceAsStream(filename);
        try {
            props.load(in);
            if (props.get(key) == null) {
                System.err.println("Could not find version property");
                VERSION = "0";
            } else {
                VERSION = props.get(key).toString();
            }

            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
