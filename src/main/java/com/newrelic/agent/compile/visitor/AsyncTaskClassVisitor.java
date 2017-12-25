//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.newrelic.agent.compile.visitor;

import com.newrelic.agent.compile.InstrumentationContext;
import com.newrelic.agent.compile.Log;
import com.google.common.collect.ImmutableMap;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class AsyncTaskClassVisitor extends ClassVisitor {
    public static final String TARGET_CLASS = "android/os/AsyncTask";
    private final InstrumentationContext context;
    private final Log log;
    private boolean instrument = false;
    public static final ImmutableMap<String, String> traceMethodMap = ImmutableMap.of("doInBackground", "([Ljava/lang/Object;)Ljava/lang/Object;");
    public static final ImmutableMap<String, String> endTraceMethodMap = ImmutableMap.of("onPostExecute", "(Ljava/lang/Object;)V");

    public AsyncTaskClassVisitor(ClassVisitor cv, InstrumentationContext context, Log log) {
        super(Opcodes.ASM5, cv);
        this.context = context;
        this.log = log;
    }

    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        if(superName != null && superName.equals(TARGET_CLASS)) {
            interfaces = TraceClassDecorator.addInterface(interfaces);
            this.log.info("[AsyncTaskClassVisitor] Added Trace interface to class[" + this.context.getClassName() + "] superName[" + superName + "]");
            super.visit(version, access, name, signature, superName, interfaces);
            this.instrument = true;
            this.log.debug("[AsyncTaskClassVisitor] Rewriting [" + this.context.getClassName() + "]");
            this.context.markModified();
        } else {
            super.visit(version, access, name, signature, superName, interfaces);
        }

    }

    public void visitEnd() {
        if(this.instrument) {
            TraceClassDecorator decorator = new TraceClassDecorator(this);
            decorator.addTraceField();
            decorator.addTraceInterface(Type.getObjectType(this.context.getClassName()));
            this.log.info("[AsyncTaskClassVisitor] Added Trace object and interface to [" + this.context.getClassName() + "]");
        }

        super.visitEnd();
    }

    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);
        if(this.instrument) {
            TraceMethodVisitor traceMethodVisitor;
            if(traceMethodMap.containsKey(name) && (traceMethodMap.get(name)).equals(desc)) {
                traceMethodVisitor = new TraceMethodVisitor(methodVisitor, access, name, desc, this.context);
                traceMethodVisitor.setUnloadContext();
                return traceMethodVisitor;
            }

            if(endTraceMethodMap.containsKey(name) && (endTraceMethodMap.get(name)).equals(desc)) {
                traceMethodVisitor = new TraceMethodVisitor(methodVisitor, access, name, desc, this.context);
                return traceMethodVisitor;
            }
        }

        return methodVisitor;
    }
}
