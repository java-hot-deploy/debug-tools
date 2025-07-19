/*
 * Copyright (C) 2024-2025 the original author or authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package io.github.future0923.debug.tools.base.utils;

import io.github.future0923.debug.tools.base.logging.AnsiLog;

import java.io.File;

/**
 * @author future0923
 */
public class DebugToolsLibUtils {


    private static File DEBUG_TOOLS_HOME_DIR;
    private static final File DEBUG_TOOLS_LIB_DIR;
    private static final File DEBUG_TOOLS_CACHE_DIR;
    private static final File DEBUG_TOOLS_CONFIG_DIR;

    static {
        String debugToolsLibDirEnv = System.getenv("DEBUG_TOOLS_HOME_DIR");
        if (debugToolsLibDirEnv != null) {
            DEBUG_TOOLS_HOME_DIR = new File(debugToolsLibDirEnv);
            AnsiLog.info("DEBUG_TOOLS_LIB_DIR: " + debugToolsLibDirEnv);
        } else {
            DEBUG_TOOLS_HOME_DIR = new File(System.getProperty("user.home") + File.separator + ".debugTools");
        }
        try {
            DEBUG_TOOLS_HOME_DIR.mkdirs();
        } catch (Throwable t) {
            //ignore
        }
        if (!DEBUG_TOOLS_HOME_DIR.exists()) {
            // try to set a temp directory
            DEBUG_TOOLS_HOME_DIR = new File(System.getProperty("java.io.tmpdir") + File.separator + ".debugTools");
            try {
                DEBUG_TOOLS_HOME_DIR.mkdirs();
            } catch (Throwable e) {
                // ignore
            }
        }
        if (!DEBUG_TOOLS_HOME_DIR.exists()) {
            System.err.println("Can not find directory to save debug tools lib. please try to set user home by -Duser.home=");
        }
        DEBUG_TOOLS_LIB_DIR = new File(DEBUG_TOOLS_HOME_DIR, "lib");
        if (!DEBUG_TOOLS_LIB_DIR.exists()) {
            try {
                DEBUG_TOOLS_LIB_DIR.mkdirs();
            } catch (Throwable e) {
                // ignore
            }
        }
        DEBUG_TOOLS_CACHE_DIR = new File(DEBUG_TOOLS_HOME_DIR, "cache");
        if (!DEBUG_TOOLS_CACHE_DIR.exists()) {
            try {
                DEBUG_TOOLS_CACHE_DIR.mkdirs();
            } catch (Throwable e) {
                // ignore
            }
        }
        DEBUG_TOOLS_CONFIG_DIR = new File(DEBUG_TOOLS_HOME_DIR, "config");
        if (!DEBUG_TOOLS_CONFIG_DIR.exists()) {
            try {
                DEBUG_TOOLS_CONFIG_DIR.mkdirs();
            } catch (Throwable e) {
                // ignore
            }
        }





    }

    public static File getDebugToolsHomeDir() {
        return DEBUG_TOOLS_HOME_DIR;
    }

    public static File getDebugToolsLibDir() {
        return DEBUG_TOOLS_LIB_DIR;
    }

    public static File getDebugToolsCacheDir() {
        return DEBUG_TOOLS_CACHE_DIR;
    }
    public static File getDebugToolsConfigDir() {
        return DEBUG_TOOLS_CONFIG_DIR;
    }

}
