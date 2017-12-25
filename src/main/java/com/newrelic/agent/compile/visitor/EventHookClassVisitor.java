//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.newrelic.agent.compile.visitor;

import com.newrelic.agent.compile.InstrumentationContext;
import com.newrelic.agent.compile.Log;
import com.newrelic.agent.compile.transformers.NewRelicClassTransformer;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class EventHookClassVisitor extends ClassVisitor {
    protected final Map<String, Pattern> baseClassPatterns;
    private final Map<Method, EventHookClassVisitor.MethodVisitorFactory> methodVisitors;
    protected String superName;
    protected boolean instrument = false;
    protected final InstrumentationContext context;
    protected final Log log;

    public EventHookClassVisitor(ClassVisitor cv, InstrumentationContext context, Log log, Set<String> baseClasses, Map<Method, Method> methodMappings) {
        super(Opcodes.ASM5, cv);
        this.context = context;
        this.log = log;
        this.methodVisitors = new HashMap<Method, EventHookClassVisitor.MethodVisitorFactory>();
        Iterator var6 = methodMappings.entrySet().iterator();

        while(var6.hasNext()) {
            Entry<Method, Method> entry = (Entry<Method, Method>) var6.next();
            this.methodVisitors.put(entry.getKey(), new EventHookClassVisitor.MethodVisitorFactory(entry.getValue()));
        }

        this.baseClassPatterns = new HashMap<String, Pattern>();
        var6 = baseClasses.iterator();

        while(var6.hasNext()) {
            String pattern = (String)var6.next();
            this.baseClassPatterns.put(pattern, Pattern.compile(pattern));
        }

    }

    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        this.superName = superName;
        this.instrument = this.shouldInstrumentClass(this.context.getClassName(), superName);
    }

    protected boolean shouldInstrumentClass(String className, String superName) {
        if(!className.startsWith(NewRelicClassTransformer.ANDROID_PACKAGE_NAME)) {
            Iterator var3 = this.baseClassPatterns.values().iterator();

            while(var3.hasNext()) {
                Pattern baseClassPattern = (Pattern)var3.next();
                Matcher matcher = baseClassPattern.matcher(superName);
                if(matcher.matches()) {
                    return true;
                }
            }
        }

        return false;
    }

    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        if(!this.instrument) {
            return mv;
        } else {
            Method method = new Method(name, desc);
            EventHookClassVisitor.MethodVisitorFactory v = this.methodVisitors.get(method);
            if(v != null) {
                this.methodVisitors.remove(method);
                return v.createMethodVisitor(access, method, mv, false);
            } else {
                return mv;
            }
        }
    }

    public void visitEnd() {
        if(this.instrument) {
            this.context.markModified();
            Iterator var1 = this.methodVisitors.entrySet().iterator();

            while(var1.hasNext()) {
                Entry<Method, EventHookClassVisitor.MethodVisitorFactory> entry = (Entry)var1.next();
                MethodVisitor mv = super.visitMethod(Opcodes.ACC_PROTECTED/*4*/, (entry.getKey()).getName(), (entry.getKey()).getDescriptor(), null, null);
                mv = (entry.getValue()).createMethodVisitor(Opcodes.ACC_PROTECTED/*4*/, entry.getKey(), mv, true);
                mv.visitCode();
                mv.visitInsn(Opcodes.RETURN/*177*/);
                mv.visitMaxs(0, 0);
                mv.visitEnd();
            }

            super.visitEnd();
        }
    }

    protected abstract void injectCodeIntoMethod(GeneratorAdapter var1, Method var2, Method var3);

    protected class MethodVisitorFactory {
        final Method monitorMethod;

        public MethodVisitorFactory(Method monitorMethod) {
            this.monitorMethod = monitorMethod;
        }

        public MethodVisitor createMethodVisitor(int access, final Method method, MethodVisitor mv, final boolean callSuper) {
            return new GeneratorAdapter(Opcodes.ASM5, mv, access, method.getName(), method.getDescriptor()) {
                public void visitCode() {
                    super.visitCode();
                    if(callSuper) {
                        this.loadThis();

                        for(int i = 0; i < method.getArgumentTypes().length; ++i) {
                            this.loadArg(i);
                        }

                        this.visitMethodInsn(Opcodes.INVOKESPECIAL/*183*/, EventHookClassVisitor.this.superName, method.getName(), method.getDescriptor(), false);
                    }

                    EventHookClassVisitor.this.injectCodeIntoMethod(this, method, MethodVisitorFactory.this.monitorMethod);
                }
            };
        }
    }
}
