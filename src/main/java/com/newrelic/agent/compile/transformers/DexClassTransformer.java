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
import com.newrelic.agent.compile.visitor.SafeInstrumentationMethodVisitor;
import com.newrelic.agent.compile.visitor.SkipInstrumentedMethodsMethodVisitor;
import com.newrelic.agent.util.BytecodeBuilder;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import java.io.File;
import java.lang.instrument.IllegalClassFormatException;
import java.net.URISyntaxException;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public final class DexClassTransformer implements NewRelicClassTransformer {
    private Log log;
    private final Map<String, ClassVisitorFactory> classVisitors = new HashMap<String, ClassVisitorFactory>();

    public DexClassTransformer(final Log log) throws URISyntaxException {
        final String agentJarPath;
        try {
            agentJarPath = RewriterAgent.getAgentJarPath();
        } catch (URISyntaxException var4) {
            log.error("Unable to get the path to the New Relic class rewriter jar", var4);
            throw var4;
        }

        this.log = log;
        classVisitors.put(DEXER_CLASS_NAME, new ClassVisitorFactory(true) {
                    public ClassVisitor create(ClassVisitor cv) {
                        return DexClassTransformer.createDexerMainClassAdapter(cv, log);
                    }
                });
        classVisitors.put(ANT_DEX_CLASS_NAME, new ClassVisitorFactory(false) {
                    public ClassVisitor create(ClassVisitor cv) {
                        return DexClassTransformer.createAntTaskClassAdapter(cv, log);
                    }
                });
        classVisitors.put(MAVEN_DEX_CLASS_NAME, new ClassVisitorFactory(true) {
                    public ClassVisitor create(ClassVisitor cv) {
                        return DexClassTransformer.createMavenClassAdapter(cv, log, agentJarPath);
                    }
                });
        classVisitors.put(PROCESS_BUILDER_CLASS_NAME, new ClassVisitorFactory(true) {
                    public ClassVisitor create(ClassVisitor cv) {
                        return DexClassTransformer.createProcessBuilderClassAdapter(cv, log);
                    }
                });

    }

    public boolean modifies(Class<?> clazz) {
        Type t = Type.getType(clazz);
        return this.classVisitors.containsKey(t.getInternalName());
    }

    public byte[] transform(ClassLoader classLoader, String className, Class<?> clazz, ProtectionDomain protectionDomain, byte[] bytes) throws IllegalClassFormatException {
        ClassVisitorFactory factory = classVisitors.get(className);
        if(factory != null) {
            if(clazz != null && !factory.isRetransformOkay()) {
                this.log.error("Cannot instrument " + className);
                return null;
            }

            try {
                ClassReader ex = new ClassReader(bytes);
                PatchedClassWriter cw = new PatchedClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES, classLoader);
                ClassVisitor adapter = factory.create(cw);
                ex.accept(adapter, ClassReader.SKIP_FRAMES );
                this.log.debug("DexTransform: Transformed[" + className + "] Bytes In[" + bytes.length + "] Bytes Out[" + cw.toByteArray().length + "]");
                return cw.toByteArray();
            } catch (SkipException var10) {
//                this.log.error("Error SkipException class " + className, var10);
            } catch (Exception var11) {
                this.log.error("Error transforming class " + className, var11);
            }
        }

        return null;
    }

    private static ClassVisitor createDexerMainClassAdapter(ClassVisitor cw, Log log) {
        return new ClassAdapterBase(log, cw, new HashMap<Method, MethodVisitorFactory>() {
            {
                this.put(new Method(DEXER_METHOD_NAME, "(Ljava/lang/String;[B)Z"), new MethodVisitorFactory() {
                    public MethodVisitor create(final MethodVisitor mv, final int access, final String name, final String desc) {
                        return new BaseMethodVisitor(mv, access, name, desc) {
                            protected void onMethodEnter() {
                                this.builder.loadInvocationDispatcher()
                                        .loadInvocationDispatcherKey(RewriterAgent.getProxyInvocationKey(DEXER_CLASS_NAME, this.methodName))
                                        .loadArgumentsArray(this.methodDesc)
                                        .invokeDispatcher(false);
                                this.checkCast(Type.getType(byte[].class));
                                this.storeArg(1);
                            }
                        };
                    }
                });
            }
        });
    }

    private static ClassVisitor createAntTaskClassAdapter(final ClassVisitor cw, final Log log) {
        String agentFileFieldName = "NewRelicAgentFile";
        final HashMap methodVisitors = new HashMap() {
            {
                this.put(new Method("preDexLibraries", "(Ljava/util/List;)V"), new MethodVisitorFactory() {
                    public MethodVisitor create(final MethodVisitor mv, final int access, final String name, final String desc) {
                        return new BaseMethodVisitor(mv, access, name, desc) {
                            protected void onMethodEnter() {
                                this.builder.loadInvocationDispatcher().loadInvocationDispatcherKey(RewriterAgent.getProxyInvocationKey("com/android/ant/DexExecTask", this.methodName)).loadArray(new Runnable() {
                                    public void run() {
                                        loadArg(0);
                                    }
                                }).invokeDispatcher(false);
                                this.loadThis();
                                this.swap();
                                this.putField(Type.getObjectType("com/android/ant/DexExecTask"), "NewRelicAgentFile", Type.getType(Object.class));
                            }
                        };
                    }
                });
                this.put(new Method("runDx", "(Ljava/util/Collection;Ljava/lang/String;Z)V"), new MethodVisitorFactory() {
                    public MethodVisitor create(final MethodVisitor mv, final int access, final String name, final String desc) {
                        return new SafeInstrumentationMethodVisitor(mv, access, name, desc) {
                            protected void onMethodEnter() {
                                this.builder.loadInvocationDispatcher().loadInvocationDispatcherKey("SET_INSTRUMENTATION_DISABLED_FLAG").loadArray(new Runnable() {
                                    public void run() {
                                        loadThis();
                                        getField(Type.getObjectType("com/android/ant/DexExecTask"), "NewRelicAgentFile", Type.getType(Object.class));
                                    }
                                }).invokeDispatcher();
                            }
                        };
                    }
                });
            }
        };
        return new ClassAdapterBase(log, cw, methodVisitors) {
            public void visitEnd() {
                super.visitEnd();
                this.visitField(2, "NewRelicAgentFile", Type.getType(Object.class).getDescriptor(), (String)null, (Object)null);
            }
        };
    }

    private static ClassVisitor createProcessBuilderClassAdapter(final ClassVisitor cw, Log log) {
        return new ClassVisitor(327680, cw) {
            public MethodVisitor visitMethod(final int access, final String name, final String desc, String signature, String[] exceptions) {
                Object mv = super.visitMethod(access, name, desc, signature, exceptions);
                if("start".equals(name)) {
                    mv = new SkipInstrumentedMethodsMethodVisitor(new BaseMethodVisitor((MethodVisitor)mv, access, name, desc) {
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

                return (MethodVisitor)mv;
            }
        };
    }

    private static ClassVisitor createMavenClassAdapter(ClassVisitor cw, Log log, final String agentJarPath) {
        HashMap methodVisitors = new HashMap() {
            {
                this.put(new Method("runDex", "(Lcom/jayway/maven/plugins/android/CommandExecutor;Ljava/io/File;Ljava/util/Set;)V"), new MethodVisitorFactory() {
                    public MethodVisitor create(final MethodVisitor mv, final int access, final String name, final String desc) {
                        return new GeneratorAdapter(327680, mv, access, name, desc) {
                            public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean isInterface) {
                                if("executeCommand".equals(name) && "(Ljava/lang/String;Ljava/util/List;Ljava/io/File;Z)V".equals(desc)) {
                                    int arg3 = this.newLocal(Type.BOOLEAN_TYPE);
                                    this.storeLocal(arg3);
                                    int arg2 = this.newLocal(Type.getType(File.class));
                                    this.storeLocal(arg2);
                                    this.dup();
                                    this.push(0);
                                    String agentCommand = "-javaagent:" + agentJarPath;
                                    if(RewriterAgent.getAgentArgs() != null) {
                                        agentCommand = agentCommand + "=" + RewriterAgent.getAgentArgs();
                                    }

                                    (new BytecodeBuilder(this)).printToInfoLogFromBytecode("Maven agent jar: " + agentCommand);
                                    this.visitLdcInsn(agentCommand);
                                    this.invokeInterface(Type.getType(List.class), new Method("add", "(ILjava/lang/Object;)V"));
                                    this.loadLocal(arg2);
                                    this.loadLocal(arg3);
                                }

                                super.visitMethodInsn(opcode, owner, name, desc, isInterface);
                            }
                        };
                    }
                });
            }
        };
        return new ClassAdapterBase(log, cw, methodVisitors);
    }
}
