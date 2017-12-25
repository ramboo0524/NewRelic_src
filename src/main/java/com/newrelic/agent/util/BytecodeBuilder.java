//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.newrelic.agent.util;

import com.newrelic.agent.compile.InvocationDispatcher;
import com.newrelic.agent.compile.RewriterAgent;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;

public final class BytecodeBuilder {
    private final GeneratorAdapter mv;

    public BytecodeBuilder(GeneratorAdapter adapter) {
        this.mv = adapter;
    }

    public BytecodeBuilder loadNull() {
        this.mv.visitInsn(Opcodes.ACONST_NULL/*1*/);
        return this;
    }

    public BytecodeBuilder loadInvocationDispatcher() {
        this.mv.visitLdcInsn(Type.getType(InvocationDispatcher.INVOCATION_DISPATCHER_CLASS));
        this.mv.visitLdcInsn(InvocationDispatcher.INVOCATION_DISPATCHER_FIELD_NAME);
        this.mv.invokeVirtual(Type.getType(Class.class), new Method("getDeclaredField", "(Ljava/lang/String;)Ljava/lang/reflect/Field;"));
        this.mv.dup();
        this.mv.visitInsn(Opcodes.ICONST_1/*4*/);
        this.mv.invokeVirtual(Type.getType(Field.class), new Method("setAccessible", "(Z)V"));
        this.mv.visitInsn(Opcodes.ACONST_NULL/*1*/);
        this.mv.invokeVirtual(Type.getType(Field.class), new Method("get", "(Ljava/lang/Object;)Ljava/lang/Object;"));
        return this;
    }

    public BytecodeBuilder loadArgumentsArray(String methodDesc) {
        Method method = new Method("dummy", methodDesc);
        this.mv.push(method.getArgumentTypes().length);
        Type objectType = Type.getType(Object.class);
        this.mv.newArray(objectType);

        for(int i = 0; i < method.getArgumentTypes().length; ++i) {
            this.mv.dup();
            this.mv.push(i);
            this.mv.loadArg(i);
            this.mv.arrayStore(objectType);
        }

        return this;
    }

    public BytecodeBuilder loadArray(Runnable... r) {
        this.mv.push(r.length);
        Type objectType = Type.getObjectType("java/lang/Object");
        this.mv.newArray(objectType);

        for(int i = 0; i < r.length; ++i) {
            this.mv.dup();
            this.mv.push(i);
            r[i].run();
            this.mv.arrayStore(objectType);
        }

        return this;
    }

    public BytecodeBuilder printToInfoLogFromBytecode(final String message) {
        this.loadInvocationDispatcher();
        this.mv.visitLdcInsn(RewriterAgent.PRINT_TO_INFO_LOG);
        this.mv.visitInsn(Opcodes.ACONST_NULL/*1*/);
        this.loadArray(new Runnable() {
            public void run() {
                BytecodeBuilder.this.mv.visitLdcInsn(message);
            }
        });
        this.invokeDispatcher();
        return this;
    }

    public BytecodeBuilder invokeDispatcher() {
        return this.invokeDispatcher(true);
    }

    public BytecodeBuilder invokeDispatcher(boolean popReturnOffStack) {
        this.mv.invokeInterface(Type.getType(InvocationHandler.class), new Method("invoke", "(Ljava/lang/Object;Ljava/lang/reflect/Method;[Ljava/lang/Object;)Ljava/lang/Object;"));
        if(popReturnOffStack) {
            this.mv.pop();
        }

        return this;
    }

    public BytecodeBuilder loadInvocationDispatcherKey(String key) {
        this.mv.visitLdcInsn(key);
        this.mv.visitInsn(1);
        return this;
    }
}
