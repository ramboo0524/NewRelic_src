//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.newrelic.agent.compile.visitor;

import com.newrelic.agent.util.AnnotationImpl;
import com.newrelic.agent.util.MethodAnnotation;
import org.objectweb.asm.*;

import java.util.ArrayList;
import java.util.Collection;

public class MethodAnnotationVisitor {
    public MethodAnnotationVisitor() {
    }

    public static Collection<MethodAnnotation> getAnnotations(ClassReader cr, String annotationDescription) {
        MethodAnnotationVisitor.MethodAnnotationClassVisitor visitor = new MethodAnnotationVisitor.MethodAnnotationClassVisitor(annotationDescription);
        cr.accept(visitor, 0);
        return visitor.getAnnotations();
    }

    private static class MethodAnnotationClassVisitor extends ClassVisitor {
        String className;
        private final String annotationDescription;
        private final Collection<MethodAnnotation> annotations = new ArrayList();

        public MethodAnnotationClassVisitor(String annotationDescription) {
            super(Opcodes.ASM5);
            this.annotationDescription = annotationDescription;
        }

        public Collection<MethodAnnotation> getAnnotations() {
            return this.annotations;
        }

        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            this.className = name;
        }

        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            return new MethodAnnotationVisitor.MethodAnnotationClassVisitor.MethodAnnotationVisitorImpl(name, desc);
        }

        private class MethodAnnotationVisitorImpl extends MethodVisitor {
            private final String methodName;
            private final String methodDesc;

            public MethodAnnotationVisitorImpl(String name, String desc) {
                super(Opcodes.ASM5);
                this.methodName = name;
                this.methodDesc = desc;
            }

            public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                if(MethodAnnotationClassVisitor.this.annotationDescription.equals(desc)) {
                    MethodAnnotationVisitor.MethodAnnotationClassVisitor.MethodAnnotationVisitorImpl.MethodAnnotationImpl annotation = new MethodAnnotationVisitor.MethodAnnotationClassVisitor.MethodAnnotationVisitorImpl.MethodAnnotationImpl(desc);
                    MethodAnnotationClassVisitor.this.annotations.add(annotation);
                    return annotation;
                } else {
                    return null;
                }
            }

            private class MethodAnnotationImpl extends AnnotationImpl implements MethodAnnotation {
                public MethodAnnotationImpl(String desc) {
                    super(desc);
                }

                public String getMethodName() {
                    return MethodAnnotationVisitorImpl.this.methodName;
                }

                public String getMethodDesc() {
                    return MethodAnnotationVisitorImpl.this.methodDesc;
                }

                public String getClassName() {
                    return MethodAnnotationClassVisitor.this.className;
                }
            }
        }
    }
}
