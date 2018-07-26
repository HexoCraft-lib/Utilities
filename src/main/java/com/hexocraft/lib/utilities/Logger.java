package com.hexocraft.lib.utilities;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/*

 Copyright 2018 hexosse

 Licensed under the Apache License, Version 2.0 (the "License")
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 */




/**
 * Simple access to the jLogger
 */
public class Logger {

    private static java.util.logging.Logger jLogger = null;


    private Logger() {
        throw new IllegalAccessError("This is a private constructor");
    }

    /**
     * Find the more appropriate jLogger
     *
     * @return Logger
     */
    private static synchronized java.util.logging.Logger get() {
        if (jLogger == null) {

            try {
                jLogger = JavaPlugin.getProvidingPlugin(Logger.class).getLogger();
            }
            catch (Exception ignored) {
                // This Exception is ignored
            }

            if (jLogger == null) {
                try {
                    jLogger = Bukkit.getLogger();
                }
                catch (Exception ignored) {
                    // This Exception is ignored
                }
            }

            if (jLogger == null) {
                jLogger = java.util.logging.Logger.getLogger("HexoCraft-Logger");
            }
        }
        return jLogger;
    }


    /**
     * Log info message
     *
     * @param string message to log
     */
    public static void log(String string) {
        get().info(() -> string);
    }

    /**
     * Log info message
     *
     * @param string message to log
     */
    public static void logInfo(String string) {
        get().info(() -> string);
    }


    /**
     * Warning info message
     *
     * @param warning message to log
     */
    public static void logWarning(String warning) {
        get().warning(() -> warning);
    }

    /**
     * Error info message
     *
     * @param error message to log
     */
    public static void logError(String error) {
        get().severe(() -> error);
    }
}
