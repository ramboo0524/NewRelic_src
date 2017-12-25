//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.newrelic.agent.compile;

import java.util.HashMap;
import java.util.Map;

public abstract class Log {
    public static Log LOGGER = new Log(new HashMap()) {
    };
    protected final int logLevel;

    public Log(Map<String, String> agentOptions) {
        String logLevelOpt = (String)agentOptions.get("loglevel");
        if(logLevelOpt != null) {
            this.logLevel = Log.LogLevel.valueOf(logLevelOpt).getValue();
        } else {
            this.logLevel = Log.LogLevel.WARN.getValue();
        }

        LOGGER = this;
    }

    public void info(String message) {
//        if(this.logLevel >= Log.LogLevel.INFO.getValue()) {
            this.log("info", message);
//        }

    }

    public void debug(String message) {
//        if(this.logLevel >= Log.LogLevel.DEBUG.getValue()) {
            synchronized(this) {
                this.log("debug", message);
            }
//        }

    }

    public void warning(String message) {
//        if(this.logLevel >= Log.LogLevel.WARN.getValue()) {
            this.log("warn", message);
//        }

    }

    public void error(String message) {
//        if(this.logLevel >= Log.LogLevel.ERROR.getValue()) {
            this.log("error", message);
//        }

    }

    protected void log(String level, String message) {
    }

    public void warning(String message, Throwable cause) {
    }

    public void error(String message, Throwable cause) {
    }

    public static enum LogLevel {
        DEBUG(5),
        VERBOSE(4),
        INFO(3),
        WARN(2),
        ERROR(1);

        private final int value;

        private LogLevel(int newValue) {
            this.value = newValue;
        }

        public int getValue() {
            return this.value;
        }
    }
}
