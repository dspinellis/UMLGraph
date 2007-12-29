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


import java.util.Arrays;
import java.util.StringTokenizer;

/**
 * A key for accessing a specific setting.
 * 
 */
public class SettingKey {
    private static final String PATH_DELIMITER = "/";
    private static final String OPTION_DELIMITER = "@";
    private static final String[] EMPTY_CONTEXT_PATH = {};

    private String[] nodePath;
    private String key;

    public SettingKey(String[] nodePath, String key) {
        this.nodePath = nodePath;
        this.key = key;
    }

    public SettingKey(String stringKey) {
        int atIndex = stringKey.indexOf(OPTION_DELIMITER);
        if (atIndex >= 0) {
            if (atIndex == stringKey.length() - 1)
                throw new IllegalArgumentException("argument: " + stringKey);
            key = stringKey.substring(atIndex + 1);
            if (atIndex == 0) {
                nodePath = EMPTY_CONTEXT_PATH;
                return;
            }
        }
        String nodePathStr = atIndex < 0 ? stringKey : stringKey.substring(0,
                atIndex);
        StringTokenizer pathSegmenter = new StringTokenizer(nodePathStr,
                PATH_DELIMITER);
        nodePath = new String[pathSegmenter.countTokens()];
        int i = 0;
        while (pathSegmenter.hasMoreTokens())
            nodePath[i++] = pathSegmenter.nextToken();
    }

    public String[] getNodePath() {
        return nodePath;
    }

    public boolean isRoot() {
        return nodePath.length == 0;
    }
    
    public boolean hasKey() {
        return key != null;
    }


    public String getKey() {
        return key;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((key == null) ? 0 : key.hashCode());
        result = prime * result + Arrays.hashCode(nodePath);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final SettingKey other = (SettingKey) obj;
        if (key == null) {
            if (other.key != null)
                return false;
        } else if (!key.equals(other.key))
            return false;
        if (!Arrays.equals(nodePath, other.nodePath))
            return false;
        return true;
    }
    
    @Override
    public String toString() {
        StringBuffer result = new StringBuffer(PATH_DELIMITER);
        for (int i = 0; i < nodePath.length; i++) {
            result.append(nodePath[i]);
            result.append(PATH_DELIMITER);
        }
        if (hasKey()) {
            result.append(OPTION_DELIMITER);
            result.append(key);
        }
        return result.toString();
    }
}