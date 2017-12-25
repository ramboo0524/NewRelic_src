//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.newrelic.agent.compile.visitor;

import com.newrelic.agent.compile.RewriterAgent;
import org.objectweb.asm.MethodVisitor;

public abstract class SafeInstrumentationMethodVisitor extends BaseMethodVisitor {
    protected SafeInstrumentationMethodVisitor(MethodVisitor mv, int access, String methodName, String desc) {
        super(mv, access, methodName, desc);
    }

    protected final void onMethodExit(int opcode) {
        this.builder.loadInvocationDispatcher().loadInvocationDispatcherKey(RewriterAgent.SET_INSTRUMENTATION_DISABLED_FLAG).loadNull().invokeDispatcher();
        super.onMethodExit(opcode);
    }
}
