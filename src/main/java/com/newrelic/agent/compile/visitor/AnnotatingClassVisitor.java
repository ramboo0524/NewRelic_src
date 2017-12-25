//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.newrelic.agent.compile.visitor;

import com.newrelic.agent.compile.InstrumentationContext;
import com.newrelic.agent.compile.Log;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

import java.text.MessageFormat;

public class AnnotatingClassVisitor extends ClassVisitor {
    private InstrumentationContext context;
    private Log log;

    public AnnotatingClassVisitor(ClassVisitor cv, InstrumentationContext context, Log log) {
        super(Opcodes.ASM5, cv);
        this.context = context;
        this.log = log;
    }

    public void visitEnd() {
        if(context.isClassModified()) {
            context.addUniqueTag(Annotations.INSTRUMENTED);
            super.visitAnnotation(Annotations.INSTRUMENTED, false);
            log.info(MessageFormat.format("[AnnotatingClassVisitor] Tagging [{0}] as instrumented", new Object[]{context.getFriendlyClassName()}));
        }

        super.visitEnd();
    }
}
