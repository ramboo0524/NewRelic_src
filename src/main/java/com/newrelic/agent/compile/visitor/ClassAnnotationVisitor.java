//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.newrelic.agent.compile.visitor;

import com.newrelic.agent.util.ClassAnnotation;
import com.newrelic.agent.util.ClassAnnotationImpl;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.Collection;

public class ClassAnnotationVisitor extends ClassVisitor {
    private final Collection<ClassAnnotation> annotations = new ArrayList();
    private String className;
    private final String annotationDescription;

    public ClassAnnotationVisitor(String annotationDescription) {
        super(Opcodes.ASM5);
        this.annotationDescription = annotationDescription;
    }

    public Collection<ClassAnnotation> getAnnotations() {
        return this.annotations;
    }

    public static Collection<ClassAnnotation> getAnnotations(ClassReader cr, String annotationDescription) {
        ClassAnnotationVisitor visitor = new ClassAnnotationVisitor(annotationDescription);
        cr.accept(visitor, 0);
        return visitor.getAnnotations();
    }

    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.className = name;
    }

    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        if(this.annotationDescription.equals(desc)) {
            ClassAnnotationImpl annotationVisitor = new ClassAnnotationImpl(this.className, desc);
            this.annotations.add(annotationVisitor);
            return annotationVisitor;
        } else {
            return null;
        }
    }
}
