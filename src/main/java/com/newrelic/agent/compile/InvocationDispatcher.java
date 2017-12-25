//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.newrelic.agent.compile;

import com.newrelic.agent.compile.ClassData;
import com.newrelic.agent.compile.ClassRemapperConfig;
import com.newrelic.agent.compile.HaltBuildException;
import com.newrelic.agent.compile.InstrumentationContext;
import com.newrelic.agent.compile.Log;
import com.newrelic.agent.compile.RewriterAgent;
import com.newrelic.agent.compile.SkipException;
import com.newrelic.agent.compile.transformers.DexClassTransformer;
import com.newrelic.agent.compile.transformers.NewRelicClassTransformer;
import com.newrelic.agent.compile.visitor.*;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class InvocationDispatcher implements InvocationHandler {
    public static final Class INVOCATION_DISPATCHER_CLASS = Logger.class;
    public static final String INVOCATION_DISPATCHER_FIELD_NAME = "treeLock";
    public static final Set<String> DX_COMMAND_NAMES = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(new String[] { "dx", "dx.bat" })));
    public static final Set<String> JAVA_NAMES = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(new String[]{"java", "java.exe"})));
    private static final Set<String> AGENT_JAR_NAMES = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(new String[]{"newrelic.android.fat.jar", "newrelic.android.jar", "obfuscated.jar"})));
    public static final HashSet<String> EXCLUDED_PACKAGES = new HashSet<String>() {
        {
            this.add("com/newrelic/agent/android");
            this.add("com/newrelic/mobile");
            this.add("com/google/gson");
            this.add("com/google/flatbuffers");
        }
    };
    private final Log log;
    private final ClassRemapperConfig config;
    private final InstrumentationContext context;
    private final Map<String, InvocationHandler> invocationHandlers;
    private boolean writeDisabledMessage = true;
    private final String agentJarPath;
    private boolean disableInstrumentation = false;

    public InvocationDispatcher(final Log log) throws IOException, ClassNotFoundException, URISyntaxException {
        this.log = log;
        this.config = new ClassRemapperConfig(log);
        this.context = new InstrumentationContext(this.config, log);
        this.agentJarPath = RewriterAgent.getAgentJarPath();
        this.invocationHandlers = Collections.unmodifiableMap(new HashMap<String, InvocationHandler>() {
            {
                String proxyInvocationKey = RewriterAgent.getProxyInvocationKey(NewRelicClassTransformer.DEXER_CLASS_NAME, NewRelicClassTransformer.DEXER_METHOD_NAME);
                this.put(proxyInvocationKey, new InvocationHandler() {
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        String filename = (String)args[0];
                        byte[] bytes = (byte[])args[1];
                        log.debug("dexer/main/processClass arg[0](filename)[" + filename + "] arg[1](bytes)[" + bytes.length + "]" + ",isInstrumentationDisabled():" + isInstrumentationDisabled());
                        if( isInstrumentationDisabled() ) {
                            if( writeDisabledMessage ) {
                                writeDisabledMessage = false;
                                log.info("Instrumentation disabled, no agent present");
                            }

                            return bytes;
                        } else {
                            writeDisabledMessage = true;
                            synchronized(context) {
                                ClassData classData = visitClassBytes(bytes);
                                if(classData != null && classData.getMainClassBytes() != null && classData.isModified()) {
                                    log.debug("dexer/main/processClass transformed bytes[" + bytes.length + "]");
                                    return classData.getMainClassBytes();
                                } else {
                                    return bytes;
                                }
                            }
                        }
                    }
                });
                proxyInvocationKey = RewriterAgent.getProxyInvocationKey("com/android/ant/DexExecTask", "preDexLibraries");
                this.put(proxyInvocationKey, new InvocationHandler() {
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        List files = (List)args[0];
                        Iterator var5 = files.iterator();

                        File file;
                        do {
                            if(!var5.hasNext()) {
                                log.debug("Ant preDexLibraries: " + files);
                                log.info("No New Relic agent detected in Ant build");
                                return null;
                            }

                            file = (File)var5.next();
                        } while(!AGENT_JAR_NAMES.contains(file.getName().toLowerCase()));

                        log.info("Detected the New Relic Android agent in an Ant build (" + file.getPath() + ")");
                        return file;
                    }
                });
                this.put(RewriterAgent.SET_INSTRUMENTATION_DISABLED_FLAG, new InvocationHandler() {
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        InvocationDispatcher.this.disableInstrumentation = args != null && args[0] == null;
                        log.debug("DisableInstrumentation: " + InvocationDispatcher.this.disableInstrumentation + " (" + args + ")");
                        return null;
                    }
                });
                this.put(RewriterAgent.PRINT_TO_INFO_LOG, new InvocationHandler() {
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        log.info(args[0].toString());
                        return null;
                    }
                });
                proxyInvocationKey = RewriterAgent.getProxyInvocationKey("java/lang/ProcessBuilder", "start");
                this.put(proxyInvocationKey, new InvocationHandler() {
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        List list = (List)args[0];
                        String command = (String)list.get(0);
                        File commandFile = new File(command);
                        if(isInstrumentationDisabled()) {
                            log.info("Instrumentation disabled, no agent present.  Command: " + commandFile.getName());
                            log.debug("Execute: " + list.toString());
                            return null;
                        } else {
                            String javaagentString = null;
                            if(DX_COMMAND_NAMES.contains(commandFile.getName().toLowerCase())) {
                                javaagentString = "-Jjavaagent:" + InvocationDispatcher.this.agentJarPath;
                            } else if(JAVA_NAMES.contains(commandFile.getName().toLowerCase())) {
                                javaagentString = "-javaagent:" + InvocationDispatcher.this.agentJarPath;
                            }

                            if(javaagentString != null) {
                                String agentArgs = RewriterAgent.getAgentArgs();
                                if(agentArgs != null) {
                                    javaagentString = javaagentString + "=" + agentArgs;
                                }

                                list.add(1, this.quoteProperty(javaagentString));
                            }

                            log.debug("processBuilder/start Execute[" + list.toString() + "]");
                            return null;
                        }
                    }

                    private String quoteProperty(String string) {
                        return System.getProperty("os.name").toLowerCase().contains("win")?"\"" + string + "\"":string;
                    }
                });
                proxyInvocationKey = RewriterAgent.getProxyInvocationKey("com/newrelic/agent/compile/ClassTransformer", "transformClassBytes");
                this.put(proxyInvocationKey, new InvocationHandler() {
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        String filename = (String)args[0];
                        byte[] bytes = (byte[])args[1];
                        if(isInstrumentationDisabled()) {
                            if(writeDisabledMessage) {
                                writeDisabledMessage = false;
                                log.info("Instrumentation disabled, no agent present");
                            }

                            return bytes;
                        } else {
                            writeDisabledMessage = true;
                            synchronized(context) {
                                log.debug("ClassTransformer/transformClassBytes arg[0](filename)[" + filename + "] arg[1](bytes)[" + bytes.length + "]");
                                ClassData classData = visitClassBytes(bytes);
                                if(context.isClassModified() && classData != null && classData.getMainClassBytes() != null) {
                                    if(bytes.length != classData.getMainClassBytes().length) {
                                        log.debug("ClassTransformer/transformClassBytes transformed bytes[" + classData.getMainClassBytes().length + "]");
                                    }

                                    return classData.getMainClassBytes();
                                } else {
                                    return null;
                                }
                            }
                        }
                    }
                });
            }
        });
    }

    private boolean isInstrumentationDisabled() {
        return disableInstrumentation || System.getProperty( RewriterAgent.DISABLE_INSTRUMENTATION_SYSTEM_PROPERTY) != null;
    }

    private boolean isExcludedPackage(String packageName) {
        String lowercasePackageName = packageName.toLowerCase();
        Iterator var3 = EXCLUDED_PACKAGES.iterator();

        String name;
        do {
            if(!var3.hasNext()) {
                return false;
            }

            name = (String)var3.next();
        } while(!lowercasePackageName.contains(name));

        return true;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        InvocationHandler handler = invocationHandlers.get(proxy);
        if(handler == null) {
            this.log.error("Unknown invocation type: " + proxy + ".  Arguments: " + Arrays.asList(args));
            return null;
        } else {
            try {
                return handler.invoke(proxy, method, args);
            } catch (Throwable var6) {
                this.log.error("Error:" + var6.getMessage(), var6);
                return null;
            }
        }
    }

    public ClassData visitClassBytes(byte[] bytes) {
        String className = "an unknown class";

        try {
            ClassReader t = new ClassReader(bytes);
            ClassWriter cw = new ClassWriter(t, 1);
            this.context.reset();
            t.accept(new PrefilterClassVisitor(this.context, this.log), 7);
            className = this.context.getClassName();
            if(!this.context.hasTag(Annotations.INSTRUMENTED)) {
                this.log.debug("[InvocationDispatcher] class [" + className + "]");
                ClassVisitor cv;
                if(className.startsWith("com/newrelic/agent/android")) {
                    cv = new NewRelicClassVisitor(cw, this.context, this.log);
                } else if(className.startsWith("android/")) {
                    cv = new ActivityClassVisitor(cw, this.context, this.log);
                } else {
                    if(this.isExcludedPackage(className)) {
                        this.log.debug("[InvocationDispatcher] Excluding class [" + className + "]");
                        return null;
                    }


                    AnnotatingClassVisitor cv1 = new AnnotatingClassVisitor(cw, this.context, this.log);
                    //ljh ActivityClassVisitor会导致运行错误，迟点解决为什么
//                    ActivityClassVisitor cv2 = new ActivityClassVisitor(cv1, this.context, this.log);
                    AsyncTaskClassVisitor cv3 = new AsyncTaskClassVisitor(cv1, this.context, this.log);
                    TraceAnnotationClassVisitor cv4 = new TraceAnnotationClassVisitor(cv3, this.context, this.log);
                    cv = new WrapMethodClassVisitor(cv4, this.context, this.log);
                }

                ContextInitializationClassVisitor cv5 = new ContextInitializationClassVisitor(cv, this.context);
                t.accept(cv5, ClassReader.EXPAND_FRAMES | ClassReader.SKIP_FRAMES/*12*/);
                if(this.context.isClassModified() && bytes.length != cw.toByteArray().length) {
                    this.log.debug("[InvocationDispatcher] class[" + className + "] bytes[" + bytes.length + "] transformed[" + cw.toByteArray().length + "]");
                }
            } else {
                this.log.warning(MessageFormat.format("[{0}] class is already instrumented! skipping ...", this.context.getFriendlyClassName()));
            }

            return this.context.newClassData(cw.toByteArray());
        } catch (SkipException var6) {
            return null;
        } catch (HaltBuildException var7) {
            throw new RuntimeException(var7);
        } catch (Throwable var8) {
            this.log.error("Unfortunately, an error has occurred while processing " + className + ". Please copy your build logs and the jar containing this class and visit http://support.newrelic.com, thanks!\n" + var8.getMessage(), var8);
            return new ClassData(bytes, false);
        }
    }

    public ClassRemapperConfig getConfig() {
        return config;
    }
}
