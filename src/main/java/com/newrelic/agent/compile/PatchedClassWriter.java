//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.newrelic.agent.compile;

import org.objectweb.asm.ClassWriter;

public class PatchedClassWriter extends ClassWriter {
    private final ClassLoader classLoader;

    public PatchedClassWriter(int flags, ClassLoader classLoader) {
        super(flags);
        this.classLoader = classLoader;
    }

    protected String getCommonSuperClass(String type1, String type2) {
        Class c;
        Class d;
        try {
            c = Class.forName(type1.replace('/', '.'), true, this.classLoader);
            d = Class.forName(type2.replace('/', '.'), true, this.classLoader);
        } catch (Exception var6) {
            throw new RuntimeException(var6.toString());
        }

        if(c.isAssignableFrom(d)) {
            return type1;
        } else if(d.isAssignableFrom(c)) {
            return type2;
        } else if(!c.isInterface() && !d.isInterface()) {
            do {
                c = c.getSuperclass();
            } while(!c.isAssignableFrom(d));

            return c.getName().replace('.', '/');
        } else {
            return "java/lang/Object";
        }
    }
}
