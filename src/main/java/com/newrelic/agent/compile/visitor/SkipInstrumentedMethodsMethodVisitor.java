//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.newrelic.agent.compile.visitor;

import com.newrelic.agent.compile.InstrumentedMethod;
import com.newrelic.agent.compile.SkipException;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class SkipInstrumentedMethodsMethodVisitor extends MethodVisitor {
    public SkipInstrumentedMethodsMethodVisitor(MethodVisitor mv) {
        super(Opcodes.ASM5, mv);
    }

    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        if(Type.getDescriptor(InstrumentedMethod.class).equals(desc)) {
            throw new SkipException();
        } else {
            return super.visitAnnotation(desc, visible);
        }
    }
}
