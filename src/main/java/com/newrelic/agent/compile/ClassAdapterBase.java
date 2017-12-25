//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.newrelic.agent.compile;

import com.newrelic.agent.compile.visitor.SkipInstrumentedMethodsMethodVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.Method;
import java.util.Map;

public class ClassAdapterBase extends ClassVisitor {

    final Map<Method, MethodVisitorFactory> methodVisitors;
    private Log log;

    public ClassAdapterBase(Log log, ClassVisitor cv, Map<Method, MethodVisitorFactory> methodVisitors) {
        super(Opcodes.ASM5, cv);
        this.methodVisitors = methodVisitors;
        this.log = log;
    }

    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        MethodVisitorFactory factory = this.methodVisitors.get(new Method(name, desc));
        return factory != null?  new SkipInstrumentedMethodsMethodVisitor(factory.create(mv, access, name, desc)) : mv;
    }
}
