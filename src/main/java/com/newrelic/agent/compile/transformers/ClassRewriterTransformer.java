//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.newrelic.agent.compile.transformers;

import com.newrelic.agent.compile.ClassAdapterBase;
import com.newrelic.agent.compile.ClassVisitorFactory;
import com.newrelic.agent.compile.Log;
import com.newrelic.agent.compile.MethodVisitorFactory;
import com.newrelic.agent.compile.PatchedClassWriter;
import com.newrelic.agent.compile.RewriterAgent;
import com.newrelic.agent.compile.SkipException;
import com.newrelic.agent.compile.transformers.NewRelicClassTransformer;
import com.newrelic.agent.compile.visitor.BaseMethodVisitor;
import com.newrelic.agent.compile.visitor.SkipInstrumentedMethodsMethodVisitor;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.Method;
import java.lang.instrument.IllegalClassFormatException;
import java.net.URISyntaxException;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.Map;

public final class ClassRewriterTransformer implements NewRelicClassTransformer {
    private Log log;
    private final Map<String, ClassVisitorFactory> classVisitors;

    public ClassRewriterTransformer(final Log log) throws URISyntaxException {
        try {
            String e = RewriterAgent.getAgentJarPath();
        } catch (URISyntaxException var3) {
            log.error("Unable to get the path to the New Relic class rewriter jar", var3);
            throw var3;
        }

        this.log = log;
        this.classVisitors = new HashMap<String, ClassVisitorFactory>();
        this.classVisitors.put("java/lang/ProcessBuilder", new ClassVisitorFactory(true) {
            public ClassVisitor create(ClassVisitor cv) {
                return ClassRewriterTransformer.createProcessBuilderClassAdapter(cv, log);
            }
        });
        this.classVisitors.put("com/newrelic/agent/compile/ClassTransformer", new ClassVisitorFactory(true) {
            public ClassVisitor create(ClassVisitor cv) {
                return ClassRewriterTransformer.createTransformClassAdapter(cv, log);
            }
        });
    }

    public boolean modifies(Class<?> clazz) {
        Type t = Type.getType(clazz);
        return this.classVisitors.containsKey(t.getInternalName());
    }

    public byte[] transform(ClassLoader classLoader, String className, Class<?> clazz, ProtectionDomain protectionDomain, byte[] bytes) throws IllegalClassFormatException {
        ClassVisitorFactory factory = this.classVisitors.get(className);
        if(factory != null) {
            if(clazz != null && !factory.isRetransformOkay()) {
                this.log.error("Cannot instrument " + className);
                return null;
            }

            try {
                ClassReader ex = new ClassReader(bytes);
                PatchedClassWriter cw = new PatchedClassWriter(3, classLoader);
                ClassVisitor adapter = factory.create(cw);
                ex.accept(adapter, 4);
                this.log.debug("ClassTransformer: Transformed[" + className + "] Bytes In[" + bytes.length + "] Bytes Out[" + cw.toByteArray().length + "]");
                return cw.toByteArray();
            } catch (SkipException var10) {
                ;
            } catch (Exception var11) {
                this.log.error("Error transforming class " + className, var11);
            }
        }

        return null;
    }

    private static ClassVisitor createProcessBuilderClassAdapter(final ClassVisitor cw, Log log) {
        return new ClassVisitor(Opcodes.ASM5, cw) {
            public MethodVisitor visitMethod(final int access, final String name, final String desc, String signature, String[] exceptions) {
                MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
                if("start".equals(name)) {
                    mv = new SkipInstrumentedMethodsMethodVisitor(new BaseMethodVisitor(mv, access, name, desc) {
                        protected void onMethodEnter() {
                            this.builder.loadInvocationDispatcher().loadInvocationDispatcherKey(RewriterAgent.getProxyInvocationKey("java/lang/ProcessBuilder", this.methodName)).loadArray(new Runnable() {
                                public void run() {
                                    loadThis();
                                    invokeVirtual(Type.getObjectType("java/lang/ProcessBuilder"), new Method("command", "()Ljava/util/List;"));
                                }
                            }).invokeDispatcher();
                        }
                    });
                }

                return mv;
            }
        };
    }

    private static ClassVisitor createTransformClassAdapter(ClassVisitor cw, Log log) {
        return new ClassAdapterBase(log, cw, new HashMap<Method, MethodVisitorFactory>() {
            {
                this.put(new Method("transformClassBytes", "(Ljava/lang/String;[B)[B"), new MethodVisitorFactory() {
                    public MethodVisitor create(final MethodVisitor mv, final int access, final String name, final String desc) {
                        return new BaseMethodVisitor(mv, access, name, desc) {
                            protected void onMethodEnter() {
                                this.builder.loadInvocationDispatcher().loadInvocationDispatcherKey(RewriterAgent.getProxyInvocationKey("com/newrelic/agent/compile/ClassTransformer", this.methodName)).loadArgumentsArray(this.methodDesc).invokeDispatcher(false);
                                this.checkCast(Type.getType(byte[].class));
                                this.storeArg(1);
                            }
                        };
                    }
                });
            }
        });
    }
}
