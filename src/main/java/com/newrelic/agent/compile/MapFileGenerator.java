//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.newrelic.agent.compile;

import com.newrelic.agent.android.instrumentation.ReplaceCallSite;
import com.newrelic.agent.android.instrumentation.TraceConstructor;
import com.newrelic.agent.android.instrumentation.WrapReturn;
import com.newrelic.agent.util.Annotations;
import com.newrelic.agent.util.MethodAnnotation;
import org.objectweb.asm.Type;
import org.reflections.util.ClasspathHelper;
import java.io.FileOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

public class MapFileGenerator {
    public MapFileGenerator() {
    }

    public static void main(String[] args) {
        if(args.length != 1) {
            System.err.println("Usage:   MapFileGenerator class_dir");
            System.exit(1);
        }

        try {
            Class.forName("com.newrelic.agent.android.Agent");
        } catch (Exception var5) {
            System.err.println("Unable to load agent classes");
            System.exit(1);
        }

        Map remapperProperties = getRemapperProperties();
        if(remapperProperties.size() == 0) {
            System.err.println("No class mappings were found");
            System.exit(1);
        }

        Iterator props = remapperProperties.entrySet().iterator();

        while(props.hasNext()) {
            Entry ex = (Entry)props.next();
            System.out.println((String)ex.getKey() + " = " + (String)ex.getValue());
        }

        Properties props1 = new Properties();
        props1.putAll(remapperProperties);

        try {
            System.out.println("Storing mapping data to " + args[0]);
            FileOutputStream ex1 = new FileOutputStream(args[0]);
            props1.store(ex1, "");
            ex1.close();
        } catch (Exception var4) {
            var4.printStackTrace();
            System.exit(1);
        }

    }

    static Map<String, String> getRemapperProperties() {
        HashMap classMap = new HashMap();
        Set urls = ClasspathHelper.forPackage("com.newrelic.agent", new ClassLoader[0]);
        Collection wrapReturnAnnotations = Annotations.getMethodAnnotations(WrapReturn.class, "com/newrelic/agent", urls);
        Iterator callSiteAnnotations = wrapReturnAnnotations.iterator();

        String typeStart;
        String typeEnd;
        String originalClassName;
        while(callSiteAnnotations.hasNext()) {
            MethodAnnotation constructorAnnotations = (MethodAnnotation)callSiteAnnotations.next();
            String annotation = (String)constructorAnnotations.getAttributes().get("className");
            String annotation1 = (String)constructorAnnotations.getAttributes().get("methodName");
            typeStart = (String)constructorAnnotations.getAttributes().get("methodDesc");
            typeEnd = constructorAnnotations.getClassName();
            originalClassName = constructorAnnotations.getMethodName();
            classMap.put(ClassRemapperConfig.WRAP_METHOD_IDENTIFIER + annotation.replace('.', '/') + '.' + annotation1 + typeStart, typeEnd + '.' + originalClassName + constructorAnnotations.getMethodDesc());
        }

        Collection var13 = Annotations.getMethodAnnotations(ReplaceCallSite.class, "com/newrelic/agent", urls);
        Iterator var14 = var13.iterator();

        String var22;
        String var23;
        while(var14.hasNext()) {
            MethodAnnotation var16 = (MethodAnnotation)var14.next();
            Boolean var18 = (Boolean)var16.getAttributes().get("isStatic");
            typeStart = (String)var16.getAttributes().get("scope");
            if(var18 == null) {
                var18 = new Boolean(false);
            }

            typeEnd = var16.getMethodName();
            originalClassName = var16.getMethodDesc();
            if(!var18.booleanValue()) {
                Type[] originalMethodDesc = Type.getArgumentTypes(originalClassName);
                Type[] newClassName = new Type[originalMethodDesc.length - 1];

                for(int newMethodName = 0; newMethodName < newClassName.length; ++newMethodName) {
                    newClassName[newMethodName] = originalMethodDesc[newMethodName + 1];
                }

                Type var24 = Type.getReturnType(originalClassName);
                originalClassName = Type.getMethodDescriptor(var24, newClassName);
            }

            var22 = var16.getClassName();
            var23 = var16.getMethodName();
            if(typeStart == null) {
                classMap.put(ClassRemapperConfig.REPLACE_CALL_SITE_IDENTIFIER + typeEnd + originalClassName, var22 + '.' + var23 + var16.getMethodDesc());
            } else {
                classMap.put(ClassRemapperConfig.REPLACE_CALL_SITE_IDENTIFIER + typeStart.replace('.', '/') + "." + typeEnd + originalClassName, var22 + '.' + var23 + var16.getMethodDesc());
            }
        }

        Collection var15 = Annotations.getMethodAnnotations(TraceConstructor.class, "com/newrelic/agent", urls);
        Iterator var17 = var15.iterator();

        while(var17.hasNext()) {
            MethodAnnotation var19 = (MethodAnnotation)var17.next();
            int var20 = var19.getMethodDesc().indexOf(")L");
            int var21 = var19.getMethodDesc().lastIndexOf(";");
            System.out.print("Start: " + var20 + " end: " + var21 + " for " + var19.getMethodDesc());
            originalClassName = var19.getMethodDesc().substring(var20 + 2, var21);
            var22 = var19.getMethodDesc().substring(0, var20 + 1) + "V";
            var23 = var19.getClassName();
            String var25 = var19.getMethodName();
            classMap.put(ClassRemapperConfig.REPLACE_CALL_SITE_IDENTIFIER + originalClassName.replace('.', '/') + "." + "<init>" + var22, var23 + '.' + var25 + var19.getMethodDesc());
        }

        return classMap;
    }
}
