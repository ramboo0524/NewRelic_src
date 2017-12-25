//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.newrelic.agent.compile.visitor;

import com.newrelic.agent.compile.InstrumentationContext;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

public class ContextInitializationClassVisitor extends ClassVisitor {
    private final InstrumentationContext context;

    public ContextInitializationClassVisitor(ClassVisitor cv, InstrumentationContext context) {
        super(Opcodes.ASM5, cv);
        this.context = context;
    }

    public void visit(int version, int access, String name, String sig, String superName, String[] interfaces) {
        this.context.setClassName(name);
        this.context.setSuperClassName(superName);
        super.visit(version, access, name, sig, superName, interfaces);
    }
}
