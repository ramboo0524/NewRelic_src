//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.newrelic.agent.compile;

import com.newrelic.agent.compile.ClassMethod;
import com.newrelic.agent.compile.Log;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

public class ClassRemapperConfig {
    public static final String WRAP_METHOD_IDENTIFIER = "WRAP_METHOD:";
    public static final String REPLACE_CALL_SITE_IDENTIFIER = "REPLACE_CALL_SITE:";
    private final Map<ClassMethod, ClassMethod> methodWrappers;
    private final Map<String, Collection<ClassMethod>> callSiteReplacements;

    public ClassRemapperConfig(Log log) throws ClassNotFoundException {
        Map<String, String> remappings = getRemappings(log);
        this.methodWrappers = getMethodWrappers(remappings, log);
        this.callSiteReplacements = getCallSiteReplacements(remappings, log);
    }

    public ClassMethod getMethodWrapper(ClassMethod method) {
        return this.methodWrappers.get(method);
    }

    public Collection<ClassMethod> getCallSiteReplacements(String className, String methodName, String methodDesc) {
        ArrayList methods = new ArrayList();
        Collection matches = this.callSiteReplacements.get(MessageFormat.format("{0}:{1}", new Object[]{methodName, methodDesc}));
        if(matches != null) {
            methods.addAll(matches);
        }

        matches = this.callSiteReplacements.get(MessageFormat.format("{0}.{1}:{2}", new Object[]{className, methodName, methodDesc}));
        if(matches != null) {
            methods.addAll(matches);
        }

        return methods;
    }

    private static Map<ClassMethod, ClassMethod> getMethodWrappers(Map<String, String> remappings, Log log) throws ClassNotFoundException {
        HashMap methodWrappers = new HashMap();
        Iterator iterator = remappings.entrySet().iterator();

        while(iterator.hasNext()) {
            Entry entry = (Entry)iterator.next();
            String key = (String) entry.getKey();
            if(key.startsWith(WRAP_METHOD_IDENTIFIER)) {
                String originalSig = key.substring(WRAP_METHOD_IDENTIFIER.length());
                log.debug("getMethodWrappers originalSig: " + originalSig + " ,(String)entry.getValue(): " + entry.getValue());
                ClassMethod origClassMethod = ClassMethod.getClassMethod(originalSig);
                ClassMethod wrappingMethod = ClassMethod.getClassMethod((String)entry.getValue());
                log.debug("getMethodWrappers origClassMethod className: " + origClassMethod.getClassName() + ",methodName: " + origClassMethod.getMethodName() + ",methodDes: " + origClassMethod.getMethodDesc()  ) ;
                log.debug("getMethodWrappers wrappingMethod className: " + wrappingMethod.getClassName() + ",methodName: " + wrappingMethod.getMethodName() + ",methodDes: " + wrappingMethod.getMethodDesc()  ) ;
                methodWrappers.put(origClassMethod, wrappingMethod);
            }
        }

        return methodWrappers;
    }

    private static Map<String, Collection<ClassMethod>> getCallSiteReplacements(Map<String, String> remappings, Log log) throws ClassNotFoundException {
        HashMap temp = new HashMap();
        Iterator callSiteReplacements = remappings.entrySet().iterator();

        while(callSiteReplacements.hasNext()) {
            Entry entry = (Entry)callSiteReplacements.next();
            if(((String)entry.getKey()).startsWith(ClassRemapperConfig.REPLACE_CALL_SITE_IDENTIFIER)) {
                String entry1 = ((String)entry.getKey()).substring(ClassRemapperConfig.REPLACE_CALL_SITE_IDENTIFIER.length());
                String methodName;
                if(entry1.contains(".")) {
                    ClassMethod nameDesc = ClassMethod.getClassMethod(entry1);
                    ClassMethod paren = ClassMethod.getClassMethod((String)entry.getValue());
                    methodName = MessageFormat.format("{0}.{1}:{2}", new Object[]{nameDesc.getClassName(), nameDesc.getMethodName(), nameDesc.getMethodDesc()});
                    Set methodDesc = (Set)temp.get(methodName);
                    if(methodDesc == null) {
                        methodDesc = new HashSet();
                        temp.put(methodName, methodDesc);
                    }

                    methodDesc.add(paren);
                } else {
                    String[] nameDesc1 = entry1.split(":");
                    int paren1 = entry1.indexOf("(");
                    methodName = entry1.substring(0, paren1);
                    String methodDesc1 = entry1.substring(paren1);
                    String key = MessageFormat.format("{0}:{1}", new Object[]{methodName, methodDesc1});
                    ClassMethod replacement = ClassMethod.getClassMethod((String)entry.getValue());
                    Object replacements = (Set)temp.get(key);
                    if(replacements == null) {
                        replacements = new HashSet();
                        temp.put(key, replacements);
                    }

                    ((Set)replacements).add(replacement);
                }
            }
        }

        HashMap callSiteReplacements1 = new HashMap();
        Iterator entry2 = temp.entrySet().iterator();

        while(entry2.hasNext()) {
            Entry entry3 = (Entry)entry2.next();
            callSiteReplacements1.put(entry3.getKey(), entry3.getValue());
        }

        return callSiteReplacements1;

    }

    private static Map getRemappings(Log log) {
        Properties props = new Properties();
        URL resource = ClassRemapperConfig.class.getResource("/type_map.properties");
        if(resource == null) {
            log.error("Unable to find the type map");
            System.exit(1);
        }

        InputStream in = null;

        try {
            in = resource.openStream();
            props.load(in);
        } catch (Throwable var13) {
            log.error("Unable to read the type map", var13);
            System.exit(1);
        } finally {
            if(in != null) {
                try {
                    in.close();
                } catch (IOException var12) {
                    ;
                }
            }

        }

        return props;
    }
}
