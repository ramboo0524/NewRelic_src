//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.newrelic.agent.compile;

import com.newrelic.agent.compile.Log.LogLevel;
import java.util.Map;

public final class SystemErrLog extends Log {
    public SystemErrLog(Map<String, String> agentOptions) {
        super(agentOptions);
    }

    protected void log(String level, String message) {
        synchronized(this) {
            System.out.println("[newrelic." + level.toLowerCase() + "] " + message);
        }
    }

    public void warning(String message, Throwable cause) {
        if(this.logLevel >= LogLevel.WARN.getValue()) {
            synchronized(this) {
                this.log("warn", message);
                cause.printStackTrace(System.err);
            }
        }

    }

    public void error(String message, Throwable cause) {
        if(this.logLevel >= LogLevel.WARN.getValue()) {
            synchronized(this) {
                this.log("error", message);
                cause.printStackTrace(System.err);
            }
        }

    }
}
