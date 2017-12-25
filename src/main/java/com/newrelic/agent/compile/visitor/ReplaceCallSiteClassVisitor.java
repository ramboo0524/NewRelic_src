//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.newrelic.agent.compile.visitor;

import com.newrelic.agent.compile.InstrumentationContext;
import com.newrelic.agent.compile.Log;
import com.google.common.collect.Sets;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.GeneratorAdapter;
import java.util.Set;

public class ReplaceCallSiteClassVisitor extends ClassVisitor {
    private final InstrumentationContext context;
    private final Log log;
    private final Set<String> recursiveCallCheckThreadLocals = Sets.newHashSet();

    public ReplaceCallSiteClassVisitor(ClassVisitor cv, InstrumentationContext context, Log log) {
        super(Opcodes.ASM5, cv);
        this.context = context;
        this.log = log;
    }

    public MethodVisitor visitMethod(int access, String name, String desc, String sig, String[] exceptions) {
        return new ReplaceCallSiteClassVisitor.MethodWrapMethodVisitor(super.visitMethod(access, name, desc, sig, exceptions), access, name, desc);
    }

    private final class MethodWrapMethodVisitor extends GeneratorAdapter {
        private final String name;
        private final String desc;
        private boolean isReplaceClassSite;

        public MethodWrapMethodVisitor(MethodVisitor mv, int access, String name, String desc) {
            super(mv, access, name, desc);
            ReplaceCallSiteClassVisitor.this.log.debug("DUDE " + name + desc);
            this.name = name;
            this.desc = desc;
        }

        public AnnotationVisitor visitAnnotation(String name, boolean arg1) {
            if("Lcom/newrelic/agent/android/instrumentation/ReplaceCallSite;".equals(name)) {
                this.isReplaceClassSite = true;
            }

            return super.visitAnnotation(name, arg1);
        }

        public void visitCode() {
            super.visitCode();
        }
    }
}
