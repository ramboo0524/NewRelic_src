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
import java.net.URL;
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

    ////ljh这里由intelij反编译，与jd-gui有出入
    static Map<String, String> getRemapperProperties() {
        Map<String, String> classMap = new HashMap();
        Set<URL> urls = ClasspathHelper.forPackage("com.newrelic.agent", new ClassLoader[0]);
        Collection<MethodAnnotation> wrapReturnAnnotations = Annotations.getMethodAnnotations(WrapReturn.class, "com/newrelic/agent", urls);
        Iterator var3 = wrapReturnAnnotations.iterator();

        String scope;
        String originalMethodName;
        String originalMethodDesc;
        while(var3.hasNext()) {
            MethodAnnotation annotation = (MethodAnnotation)var3.next();
            String originalClassName = (String)annotation.getAttributes().get("className");
            originalMethodName = (String)annotation.getAttributes().get("methodName");
            scope = (String)annotation.getAttributes().get("methodDesc");
            originalMethodName = annotation.getClassName();
            originalMethodDesc = annotation.getMethodName();
            classMap.put("WRAP_METHOD:" + originalClassName.replace('.', '/') + '.' + originalMethodName + scope, originalMethodName + '.' + originalMethodDesc + annotation.getMethodDesc());
        }

        Collection<MethodAnnotation> callSiteAnnotations = Annotations.getMethodAnnotations(ReplaceCallSite.class, "com/newrelic/agent", urls);
        Iterator var14 = callSiteAnnotations.iterator();

        String newClassName;
        String newMethodName;
        while(var14.hasNext()) {
            MethodAnnotation annotation = (MethodAnnotation)var14.next();
            Boolean isStatic = (Boolean)annotation.getAttributes().get("isStatic");
            scope = (String)annotation.getAttributes().get("scope");
            if(isStatic == null) {
                isStatic = new Boolean(false);
            }

            originalMethodName = annotation.getMethodName();
            originalMethodDesc = annotation.getMethodDesc();
            if(!isStatic.booleanValue()) {
                Type[] argTypes = Type.getArgumentTypes(originalMethodDesc);
                Type[] newArgTypes = new Type[argTypes.length - 1];

                for(int i = 0; i < newArgTypes.length; ++i) {
                    newArgTypes[i] = argTypes[i + 1];
                }

                Type returnType = Type.getReturnType(originalMethodDesc);
                originalMethodDesc = Type.getMethodDescriptor(returnType, newArgTypes);
            }

            newClassName = annotation.getClassName();
            newMethodName = annotation.getMethodName();
            if(scope == null) {
                classMap.put("REPLACE_CALL_SITE:" + originalMethodName + originalMethodDesc, newClassName + '.' + newMethodName + annotation.getMethodDesc());
            } else {
                classMap.put("REPLACE_CALL_SITE:" + scope.replace('.', '/') + "." + originalMethodName + originalMethodDesc, newClassName + '.' + newMethodName + annotation.getMethodDesc());
            }
        }

        Collection<MethodAnnotation> constructorAnnotations = Annotations.getMethodAnnotations(TraceConstructor.class, "com/newrelic/agent", urls);
        Iterator var17 = constructorAnnotations.iterator();

        while(var17.hasNext()) {
            MethodAnnotation annotation = (MethodAnnotation)var17.next();
            int typeStart = annotation.getMethodDesc().indexOf(")L");
            int typeEnd = annotation.getMethodDesc().lastIndexOf(";");
            System.out.print("Start: " + typeStart + " end: " + typeEnd + " for " + annotation.getMethodDesc());
            String originalClassName = annotation.getMethodDesc().substring(typeStart + 2, typeEnd);

            originalMethodDesc = annotation.getMethodDesc().substring(0, typeStart + 1) + "V";
            newClassName = annotation.getClassName();
            newMethodName = annotation.getMethodName();

            classMap.put("REPLACE_CALL_SITE:" + originalClassName.replace('.', '/') + "." + "<init>" + originalMethodDesc, newClassName + '.' + newMethodName + annotation
                    .getMethodDesc());
        }

        return classMap;
    }
}
