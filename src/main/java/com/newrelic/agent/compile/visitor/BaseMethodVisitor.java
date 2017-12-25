//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.newrelic.agent.compile.visitor;

import com.newrelic.agent.compile.InstrumentedMethod;
import com.newrelic.agent.util.BytecodeBuilder;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;

public abstract class BaseMethodVisitor extends AdviceAdapter {
    protected String methodName;
    protected final BytecodeBuilder builder = new BytecodeBuilder(this);

    protected BaseMethodVisitor(MethodVisitor mv, int access, String methodName, String desc) {
        super(Opcodes.ASM5, mv, access, methodName, desc);
        this.methodName = methodName;
    }

    public void visitEnd() {
        super.visitAnnotation(Type.getDescriptor(InstrumentedMethod.class), false);
        super.visitEnd();
    }
}
