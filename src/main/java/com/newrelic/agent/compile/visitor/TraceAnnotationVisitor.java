//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.newrelic.agent.compile.visitor;

import com.newrelic.agent.compile.InstrumentationContext;
import com.newrelic.agent.compile.Log;
import com.newrelic.agent.util.AnnotationImpl;
import org.objectweb.asm.Type;

public class TraceAnnotationVisitor extends AnnotationImpl {
    final Log log;
    final InstrumentationContext context;

    public TraceAnnotationVisitor(String name, InstrumentationContext context) {
        super(name);
        this.context = context;
        this.log = context.getLog();
    }

    public void visitEnum(String parameterName, String desc, String value) {
        super.visitEnum(parameterName, desc, value);
        String className = Type.getType(desc).getClassName();
        this.context.addTracedMethodParameter(this.getName(), parameterName, className, value);
    }

    public void visit(String parameterName, Object value) {
        super.visit(parameterName, value);
        String className = value.getClass().getName();
        this.context.addTracedMethodParameter(this.getName(), parameterName, className, value.toString());
    }
}
