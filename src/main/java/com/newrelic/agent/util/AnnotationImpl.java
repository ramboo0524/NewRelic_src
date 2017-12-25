//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.newrelic.agent.util;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class AnnotationImpl extends AnnotationVisitor {
    private final String name;
    private Map<String, Object> attributes;

    public AnnotationImpl(String name) {
        super(Opcodes.ASM5);
        this.name = name;
    }

    public void visitEnum(String name, String desc, String value) {
        if(this.attributes == null) {
            this.attributes = new HashMap<String, Object>();
        }

        this.attributes.put(name, value);
    }

    public void visitEnd() {
    }

    public AnnotationVisitor visitArray(String name) {
        return new AnnotationImpl.ArrayVisitor(name);
    }

    public AnnotationVisitor visitAnnotation(String name, String desc) {
        return null;
    }

    public void visit(String name, Object value) {
        if(this.attributes == null) {
            this.attributes = new HashMap<String, Object>();
        }

        this.attributes.put(name, value);
    }

    public String getName() {
        return this.name;
    }

    public Map<String, Object> getAttributes() {
        return this.attributes == null ? /*Collections.emptyMap()*/new HashMap<String, Object>() : this.attributes;
    }

    private final class ArrayVisitor extends AnnotationVisitor {
        private final String name;
        private final ArrayList<Object> values = new ArrayList<Object>();

        public ArrayVisitor(String name) {
            super(Opcodes.ASM5);
            this.name = name;
        }

        public void visit(String name, Object value) {
            this.values.add(value);
        }

        public AnnotationVisitor visitAnnotation(String arg0, String arg1) {
            return null;
        }

        public AnnotationVisitor visitArray(String name) {
            return null;
        }

        public void visitEnd() {
            this.visit(this.name, this.values.toArray(new String[0]));
        }

        public void visitEnum(String arg0, String arg1, String arg2) {
        }
    }
}
