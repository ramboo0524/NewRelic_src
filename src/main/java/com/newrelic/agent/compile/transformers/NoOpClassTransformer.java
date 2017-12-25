//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.newrelic.agent.compile.transformers;

import org.objectweb.asm.Type;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.HashSet;

public final class NoOpClassTransformer implements NewRelicClassTransformer {
    private static HashSet<String> classVisitors = new HashSet<String>() {
        {
            this.add(DEXER_CLASS_NAME);
            this.add(ANT_DEX_CLASS_NAME);
            this.add(MAVEN_DEX_CLASS_NAME);
            this.add(PROCESS_BUILDER_CLASS_NAME);
            this.add(NR_CLASS_REWRITER_CLASS_NAME);
        }
    };

    public NoOpClassTransformer() {
    }

    public byte[] transform(ClassLoader classLoader, String s, Class<?> aClass, ProtectionDomain protectionDomain, byte[] bytes) throws IllegalClassFormatException {
        return null;
    }

    public boolean modifies(Class<?> clazz) {
        Type t = Type.getType(clazz);
        return classVisitors.contains(t.getInternalName());
    }
}
