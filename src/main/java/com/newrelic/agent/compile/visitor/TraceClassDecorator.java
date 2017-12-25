//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.newrelic.agent.compile.visitor;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;
import java.util.ArrayList;
import java.util.Arrays;

public class TraceClassDecorator {
    public static final String TRACE_FIELD_INTERFACE_CLASS = "com/newrelic/agent/android/api/v2/TraceFieldInterface";
    public static final String LCOM_NEWRELIC_AGENT_ANDROID_TRACING_TRACE = "Lcom/newrelic/agent/android/tracing/Trace;";
    public static final String NR_TRACE = "_nr_trace";
    private ClassVisitor adapter;

    public TraceClassDecorator(ClassVisitor adapter) {
        this.adapter = adapter;
    }

    public void addTraceField() {
        this.adapter.visitField(Opcodes.ACC_PUBLIC, NR_TRACE, LCOM_NEWRELIC_AGENT_ANDROID_TRACING_TRACE, null, null);
    }

    public static String[] addInterface(String[] interfaces) {
        ArrayList<String> newInterfaces = new ArrayList<String>(Arrays.asList(interfaces));
        newInterfaces.remove(TRACE_FIELD_INTERFACE_CLASS);
        newInterfaces.add(TRACE_FIELD_INTERFACE_CLASS);
        return newInterfaces.toArray(new String[newInterfaces.size()]);
    }

    //ljh 这里修改了。
    public void addTraceInterface(final Type ownerType) {
        Method method = new Method("_nr_setTrace", "(Lcom/newrelic/agent/android/tracing/Trace;)V");
        MethodVisitor mv = this.adapter.visitMethod(Opcodes.ACC_PUBLIC, method.getName(), method.getDescriptor(), null, null);
//        MethodVisitor mv = new GeneratorAdapter(327680, mv, 1, method.getName(), method.getDescriptor()) {
        mv = new GeneratorAdapter(Opcodes.ASM5, mv, Opcodes.ACC_PUBLIC, method.getName(), method.getDescriptor()) {
            public void visitCode() {
                Label tryStart = new Label();
                Label tryEnd = new Label();
                Label tryHandler = new Label();
                super.visitCode();
                this.visitLabel(tryStart);
                this.loadThis();
                this.loadArgs();
                this.putField(ownerType, NR_TRACE, Type.getType(LCOM_NEWRELIC_AGENT_ANDROID_TRACING_TRACE));
                this.goTo(tryEnd);
                this.visitLabel(tryHandler);
                this.pop();
                this.visitLabel(tryEnd);
                this.visitTryCatchBlock(tryStart, tryEnd, tryHandler, "java/lang/Exception");
                this.visitInsn(Opcodes.RETURN );

            }
        };
        mv.visitCode();
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
}
