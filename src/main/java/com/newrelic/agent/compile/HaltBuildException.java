//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.newrelic.agent.compile;

public class HaltBuildException extends RuntimeException {
    public HaltBuildException(String message) {
        super(message);
    }

    public HaltBuildException(Exception e) {
        super(e);
    }

    public HaltBuildException(String message, Exception e) {
        super(message, e);
    }
}
