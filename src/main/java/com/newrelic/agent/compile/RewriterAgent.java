//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.newrelic.agent.compile;

import com.newrelic.agent.compile.transformers.ClassRewriterTransformer;
import com.newrelic.agent.compile.transformers.DexClassTransformer;
import com.newrelic.agent.compile.transformers.NewRelicClassTransformer;
import com.newrelic.agent.compile.transformers.NoOpClassTransformer;
import com.newrelic.agent.util.Streams;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class RewriterAgent {
    public static final String VERSION = "5.15.2";
    public static final String DISABLE_INSTRUMENTATION_SYSTEM_PROPERTY = "newrelic.instrumentation.disabled";
    public static final String SET_INSTRUMENTATION_DISABLED_FLAG = "SET_INSTRUMENTATION_DISABLED_FLAG";
    public static final String PRINT_TO_INFO_LOG = "PRINT_TO_INFO_LOG";
    private static String agentArgs;
    private static Map<String, String> agentOptions = Collections.emptyMap();

    public RewriterAgent() {
    }

    public static void premain(String agentArgs, Instrumentation instrumentation) {
        Throwable argsError = null;
        RewriterAgent.agentArgs = agentArgs;

        try {
            agentOptions = parseAgentArgs(agentArgs);
        } catch (Throwable var15) {
            argsError = var15;
        }

        String logFileName = agentOptions.get("logfile");
        Log log = logFileName == null?new SystemErrLog(agentOptions):new FileLogImpl(agentOptions, logFileName);
        if(argsError != null) {
            log.error("Agent args error: " + agentArgs, argsError);
        }

        String nameOfRunningVM = ManagementFactory.getRuntimeMXBean().getName();
        int p = nameOfRunningVM.indexOf(64);
        String pid = nameOfRunningVM.substring(0, p);
        log.debug("Bootstrapping New Relic Android class rewriter");
        log.debug("Agent args[" + agentArgs + "]");
        log.debug("Agent running in pid " + pid + " arguments: " + agentArgs);

        try {
            NewRelicClassTransformer classTransformer;
            if(agentOptions.containsKey("deinstrument")) {
                log.info("Deinstrumenting...");
                classTransformer = new NoOpClassTransformer();
            } else {
                if(agentOptions.containsKey("classTransformer")) {
                    log.info("Using class transformer.");
                    classTransformer = new ClassRewriterTransformer(log);
                } else {
                    log.info("Using DEX transformer.");
                    classTransformer = new DexClassTransformer(log);
                }
                createInvocationDispatcher(log);
            }

            instrumentation.addTransformer(classTransformer, true);
            List<Class<?>> classes = new ArrayList();
            Class[] var10 = instrumentation.getAllLoadedClasses();
            int var11 = var10.length;

            for(int var12 = 0; var12 < var11; ++var12) {
                Class<?> clazz = var10[var12];
                if((classTransformer).modifies(clazz)) {
                    classes.add(clazz);
                }
            }

            if(!classes.isEmpty()) {
                if(instrumentation.isRetransformClassesSupported()) {
                    instrumentation.retransformClasses(classes.toArray(new Class[classes.size()]));
                } else {
                    log.warning("Unable to retransform classes: " + classes);
                }
            }

            if(!agentOptions.containsKey("deinstrument")) {
                redefineClass(instrumentation, classTransformer, ProcessBuilder.class);
            }

        } catch (Throwable var14) {
            log.error("Agent startup error", var14);
            throw new RuntimeException(var14);
        }
    }

    public static void agentmain(String agentArgs, Instrumentation instrumentation) {
        premain(agentArgs, instrumentation);
    }

    public static String getVersion() {
        return VERSION;
    }

    public static Map<String, String> getAgentOptions() {
        return agentOptions;
    }

    public static String getAgentArgs() {
        return agentArgs;
    }

    public static String getProxyInvocationKey(String className, String methodName) {
        return className + "." + methodName;
    }

    private static void redefineClass(Instrumentation instrumentation, ClassFileTransformer classTransformer, Class<?> klass) throws IOException, IllegalClassFormatException, ClassNotFoundException, UnmodifiableClassException {
        String internalClassName = klass.getName().replace('.', '/');
        String classPath = internalClassName + ".class";
        ClassLoader cl = klass.getClassLoader() == null?RewriterAgent.class.getClassLoader():klass.getClassLoader();
        InputStream stream = cl.getResourceAsStream(classPath);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Streams.copy(stream, output);
        stream.close();
        byte[] newBytes = classTransformer.transform(klass.getClassLoader(), internalClassName, klass, (ProtectionDomain)null, output.toByteArray());
        ClassDefinition def = new ClassDefinition(klass, newBytes);
        instrumentation.redefineClasses(new ClassDefinition[]{def});
    }

    public static Map<String, String> parseAgentArgs(String agentArgs) {
        if(agentArgs == null) {
            return Collections.emptyMap();
        } else {
            Map<String, String> options = new HashMap();
            String[] var2 = agentArgs.split(";");
            int var3 = var2.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                String arg = var2[var4];
                String[] keyValue = arg.split("=");
                if(keyValue.length != 2) {
                    throw new IllegalArgumentException("Invalid argument: " + arg);
                }

                options.put(keyValue[0], keyValue[1]);
            }

            return options;
        }
    }

    public static String getAgentJarPath() throws URISyntaxException {
        return (new File(RewriterAgent.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath())).getAbsolutePath();
    }

    private static void createInvocationDispatcher(Log log) throws Exception {
        Field field = InvocationDispatcher.INVOCATION_DISPATCHER_CLASS.getDeclaredField( InvocationDispatcher.INVOCATION_DISPATCHER_FIELD_NAME );
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & -17);
        if(field.get((Object)null) instanceof InvocationDispatcher) {
//            InvocationDispatcher dispatcher = (InvocationDispatcher) field.get((Object)null);
//            new InvocationDispatcher(log);
//            log.info("Detected cached instrumentation. getRemappings().size(): " +  dispatcher.getConfig().getRemappings().size() );
        } else {
            field.set((Object)null, new InvocationDispatcher(log));
        }

    }
}
