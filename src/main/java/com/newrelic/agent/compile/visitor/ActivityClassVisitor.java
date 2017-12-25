//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.newrelic.agent.compile.visitor;

import com.newrelic.agent.compile.InstrumentationContext;
import com.newrelic.agent.compile.Log;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.UnmodifiableIterator;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

public class ActivityClassVisitor extends EventHookClassVisitor {
    static final String ANDROID_PACKAGE = "android/";
    static final ImmutableSet<String> ACTIVITY_CLASS_NAMES = ImmutableSet.of();
    static final ImmutableSet<String> BASE_CLASS_NAMES = ImmutableSet.of("^(android\\/.*\\/)(.*Activity)", "^(android\\/app\\/)(ActivityGroup)", "^(android\\/.*\\/)(.*Activity)([DGH].*)", "^(android\\/.*\\/)(.*Fragment)", "^(android\\/support\\/v%d\\/.*\\/)(.*FragmentCompat)");
    static final ImmutableSet<String> IGNORED_SDK_PACKAGES = ImmutableSet.of("android/app/", "android/preference/", "android/support/v", "android/support/constraint");
    static final Type applicationStateMonitorType = Type.getObjectType("com/newrelic/agent/android/background/ApplicationStateMonitor");
    public static final ImmutableMap<String, String> traceMethodMap = ImmutableMap.of("onCreate", "(Landroid/os/Bundle;)V", "onCreateView", "(Landroid/view/LayoutInflater;Landroid/view/ViewGroup;Landroid/os/Bundle;)Landroid/view/View;");
    public static final ImmutableSet<String> startTracingOn = ImmutableSet.of("onCreate");
    private boolean addDecoratedTraceField = false;

    public ActivityClassVisitor(ClassVisitor cv, InstrumentationContext context, Log log) {
        super(cv, context, log, BASE_CLASS_NAMES, ImmutableMap.of(new Method("onStart", "()V"), new Method("activityStarted", "()V"), new Method("onStop", "()V"), new Method("activityStopped", "()V")));
        this.addDecoratedTraceField = false;
    }

    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.addDecoratedTraceField = false;
        if(this.shouldInstrumentClass(name, superName)) {
            interfaces = TraceClassDecorator.addInterface(interfaces);
            this.log.info("[ActivityClassVisitor] Added Trace interface to class[" + this.context.getClassName() + "] superName[" + superName + "]");
        }

        super.visit(version, access, name, signature, superName, interfaces);
    }

    protected void injectCodeIntoMethod(GeneratorAdapter generatorAdapter, Method method, Method monitorMethod) {
        generatorAdapter.invokeStatic(applicationStateMonitorType, new Method("getInstance", applicationStateMonitorType, new Type[0]));
        generatorAdapter.invokeVirtual(applicationStateMonitorType, monitorMethod);
    }

    public MethodVisitor visitMethod(int access, String methodName, String desc, String signature, String[] exceptions) {
        String contextClassName = this.context.getClassName();
        if(this.instrument) {
            if(traceMethodMap.containsKey(methodName) && (traceMethodMap.get(methodName)).equals(desc)) {
                this.log.info("[ActivityClassVisitor] Tracing method [" + methodName + "]");
                MethodVisitor methodVisitor = super.visitMethod(access, methodName, desc, signature, exceptions);
                TraceMethodVisitor traceMethodVisitor = new TraceMethodVisitor(methodVisitor, access, methodName, desc, this.context);
                if(startTracingOn.contains(methodName)) {
                    this.log.debug("[ActivityClassVisitor] Start new trace for [" + methodName + "]");
                    traceMethodVisitor.setStartTracing();
                }

                this.addDecoratedTraceField = true;
                return traceMethodVisitor;
            }
        } else if(contextClassName.startsWith(ANDROID_PACKAGE)) {
            ;
        }

        return super.visitMethod(access, methodName, desc, signature, exceptions);
    }

    public void visitEnd() {
        if(this.instrument && this.addDecoratedTraceField) {
            TraceClassDecorator decorator = new TraceClassDecorator(this);
            decorator.addTraceField();
            this.log.debug("[ActivityClassVisitor] Added Trace object to " + this.context.getClassName());
        }

        super.visitEnd();
    }

    private boolean isSupportClass(String className) {
        UnmodifiableIterator var2 = IGNORED_SDK_PACKAGES.iterator();

        String sdk;
        do {
            if(!var2.hasNext()) {
                return false;
            }

            sdk = (String)var2.next();
        } while(!className.startsWith(sdk));

        return true;
    }
}
