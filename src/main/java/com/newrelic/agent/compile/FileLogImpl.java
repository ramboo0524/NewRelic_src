//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.newrelic.agent.compile;

import com.newrelic.agent.compile.Log.LogLevel;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Map;

final class FileLogImpl extends Log {
    private final PrintWriter writer;

    public FileLogImpl(Map<String, String> agentOptions, String logFileName) {
        super(agentOptions);

        try {
            this.writer = new PrintWriter(new FileOutputStream(logFileName));
        } catch (FileNotFoundException var4) {
            throw new RuntimeException(var4);
        }
    }

    protected void log(String level, String message) {
        synchronized(this) {
            this.writer.write("[newrelic." + level.toLowerCase() + "] " + message + "\n");
            this.writer.flush();
        }
    }

    public void warning(String message, Throwable cause) {
        if(this.logLevel >= LogLevel.WARN.getValue()) {
            this.log("warn", message);
            cause.printStackTrace(this.writer);
            this.writer.flush();
        }

    }

    public void error(String message, Throwable cause) {
        if(this.logLevel >= LogLevel.ERROR.getValue()) {
            this.log("error", message);
            cause.printStackTrace(this.writer);
            this.writer.flush();
        }

    }
}
