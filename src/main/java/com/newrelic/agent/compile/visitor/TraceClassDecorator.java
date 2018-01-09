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
    private ClassVisitor adapter;

    public TraceClassDecorator(ClassVisitor adapter) {
        this.adapter = adapter;
    }

    public void addTraceField() {
        this.adapter.visitField(1, "_nr_trace", "Lcom/newrelic/agent/android/tracing/Trace;", (String)null, (Object)null);
    }

    public static String[] addInterface(String[] interfaces) {
        ArrayList<String> newInterfaces = new ArrayList(Arrays.asList(interfaces));
        newInterfaces.remove("com/newrelic/agent/android/api/v2/TraceFieldInterface");
        newInterfaces.add("com/newrelic/agent/android/api/v2/TraceFieldInterface");
        return (String[])newInterfaces.toArray(new String[newInterfaces.size()]);
    }

    //ljh 这里修改了。
    public void addTraceInterface(final Type ownerType) {
        Method method = new Method("_nr_setTrace", "(Lcom/newrelic/agent/android/tracing/Trace;)V");
        MethodVisitor mv = this.adapter.visitMethod(1, method.getName(), method.getDescriptor(), (String)null, (String[])null);
//        MethodVisitor mv = new GeneratorAdapter(327680, mv, 1, method.getName(), method.getDescriptor()) {
        mv = new GeneratorAdapter(327680, mv, 1, method.getName(), method.getDescriptor()) {
            public void visitCode() {
                Label tryStart = new Label();
                Label tryEnd = new Label();
                Label tryHandler = new Label();
                super.visitCode();
                this.visitLabel(tryStart);
                this.loadThis();
                this.loadArgs();
                this.putField(ownerType, "_nr_trace", Type.getType("Lcom/newrelic/agent/android/tracing/Trace;"));
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
