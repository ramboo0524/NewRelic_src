//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.newrelic.agent.compile.visitor;

import com.newrelic.agent.compile.InstrumentationContext;
import com.newrelic.agent.compile.Log;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;
import org.objectweb.asm.commons.Method;
import java.util.ArrayList;
import java.util.Iterator;

public class TraceMethodVisitor extends AdviceAdapter {
    public static final String TRACE_MACHINE_INTERNAL_CLASSNAME = "com/newrelic/agent/android/tracing/TraceMachine";
    public static final String TRACE_MACHINE_ENTER_METHOD = "enterMethod";
    public static final String TRACE_MACHINE_ENTER_METHOD_DESC = "(Lcom/newrelic/agent/android/tracing/Trace;Ljava/lang/String;Ljava/util/ArrayList;)V";

    protected final InstrumentationContext context;
    protected final Log log;
    private String name;
    private boolean unloadContext = false;
    private boolean startTracing = false;
    private int access;

    public TraceMethodVisitor(MethodVisitor mv, int access, String name, String desc, InstrumentationContext context) {
        super(Opcodes.ASM5, mv, access, name, desc);
        this.access = access;
        this.context = context;
        this.log = context.getLog();
        this.name = name;
    }

    public void setUnloadContext() {
        this.unloadContext = true;
    }

    public void setStartTracing() {
        this.startTracing = true;
    }

    protected void onMethodEnter() {
        Type targetType = Type.getObjectType(TRACE_MACHINE_INTERNAL_CLASSNAME);
        if(this.startTracing) {
            super.visitLdcInsn(this.context.getSimpleClassName());
            this.log.debug("[Tracing] Start tracing [" + this.context.getSimpleClassName() + "]");
            super.invokeStatic(targetType, new Method("startTracing", "(Ljava/lang/String;)V"));
        }

        if((this.access & Opcodes.ACC_STATIC/*8*/) != 0) {
            this.log.debug("[Tracing] Static method [" + this.context.getClassName() + "#" + this.name + "]");
            super.visitInsn(Opcodes.ACC_PUBLIC/*1*/);
            super.visitLdcInsn(this.context.getSimpleClassName() + "#" + this.name);
            this.emitAnnotationParamsList(this.name);
            super.invokeStatic(targetType, new Method(TRACE_MACHINE_ENTER_METHOD, TRACE_MACHINE_ENTER_METHOD_DESC));
        } else {
            this.log.info("[Tracing] Instrumenting method [" + this.context.getClassName() + "#" + this.name + "]");
            Label tryStart = new Label();
            Label tryEnd = new Label();
            Label tryHandler = new Label();
            this.log.debug("[Tracing] [" + this.name + "] calls enterMethod()");
            super.visitLabel(tryStart);
            super.loadThis();
            super.getField(Type.getObjectType(this.context.getClassName()), "_nr_trace", Type.getType("Lcom/newrelic/agent/android/tracing/Trace;"));
            super.visitLdcInsn(this.context.getSimpleClassName() + "#" + this.name);
            this.emitAnnotationParamsList(this.name);
            super.invokeStatic(targetType, new Method(TRACE_MACHINE_ENTER_METHOD, TRACE_MACHINE_ENTER_METHOD_DESC));
            super.goTo(tryEnd);
            super.visitLabel(tryHandler);
            super.pop();
            super.visitInsn(Opcodes.ACONST_NULL/*1*/);
            super.visitLdcInsn(this.context.getSimpleClassName() + "#" + this.name);
            this.emitAnnotationParamsList(this.name);
            super.invokeStatic(targetType, new Method(TRACE_MACHINE_ENTER_METHOD, TRACE_MACHINE_ENTER_METHOD_DESC));
            super.visitLabel(tryEnd);
            super.visitTryCatchBlock(tryStart, tryEnd, tryHandler, "java/lang/NoSuchFieldError");
        }

    }

    private void emitAnnotationParamsList(String name) {
        ArrayList<String> annotationParameters = this.context.getTracedMethodParameters(name);
        if(annotationParameters != null && annotationParameters.size() != 0) {
            Method constructor = Method.getMethod("void <init> ()");
            Method add = Method.getMethod("boolean add(java.lang.Object)");
            Type arrayListType = Type.getObjectType("java/util/ArrayList");
            super.newInstance(arrayListType);
            super.dup();
            super.invokeConstructor(arrayListType, constructor);
            Iterator var6 = annotationParameters.iterator();

            while(var6.hasNext()) {
                String parameterEntry = (String)var6.next();
                super.dup();
                super.visitLdcInsn(parameterEntry);
                super.invokeVirtual(arrayListType, add);
                super.pop();
            }

        } else {
            super.visitInsn(1);
        }
    }

    protected void onMethodExit(int opcode) {
        Type targetType = Type.getObjectType(TRACE_MACHINE_INTERNAL_CLASSNAME);
        super.invokeStatic(targetType, new Method("exitMethod", "()V"));
        this.log.debug("[Tracing] [" + this.name + "] calls exitMethod()");
        if(this.unloadContext) {
            super.loadThis();
            targetType = Type.getObjectType(TRACE_MACHINE_INTERNAL_CLASSNAME);
            super.invokeStatic(targetType, new Method("unloadTraceContext", "(Ljava/lang/Object;)V"));
        }

    }
}
