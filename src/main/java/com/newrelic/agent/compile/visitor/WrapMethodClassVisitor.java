//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.newrelic.agent.compile.visitor;

import com.newrelic.agent.compile.ClassMethod;
import com.newrelic.agent.compile.InstrumentationContext;
import com.newrelic.agent.compile.Log;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Iterator;

public class WrapMethodClassVisitor extends ClassVisitor {
    private final InstrumentationContext context;
    private final Log log;

    public WrapMethodClassVisitor(ClassVisitor cv, InstrumentationContext context, Log log) {
        super(Opcodes.ASM5, cv);
        this.context = context;
        this.log = log;
    }

    public MethodVisitor visitMethod(int access, String name, String desc, String sig, String[] exceptions) {
        return (context.isSkippedMethod(name, desc) ?
                super.visitMethod(access, name, desc, sig, exceptions) :
                new WrapMethodClassVisitor.MethodWrapMethodVisitor(
                        super.visitMethod(access, name, desc, sig, exceptions), access, name, desc, this.context, this.log));
    }

    private static final class MethodWrapMethodVisitor extends GeneratorAdapter {
        private final String name;
        private final String desc;
        private final InstrumentationContext context;
        private final Log log;
        private boolean newInstructionFound = false;
        private boolean dupInstructionFound = false;

        public MethodWrapMethodVisitor(MethodVisitor mv, int access, String name, String desc, InstrumentationContext context, Log log) {
            super(Opcodes.ASM5, mv, access, name, desc);
            this.name = name;
            this.desc = desc;
            this.context = context;
            this.log = log;
        }

        public void visitMethodInsn(int opcode, String owner, String name, String desc) {
            this.visitMethodInsn(opcode, owner, name, desc, opcode == Opcodes.INVOKEINTERFACE/*185*/);
        }

        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean isInterface) {
            if (opcode == Opcodes.INVOKEDYNAMIC) {
                this.log.warning(MessageFormat.format("[{0}] INVOKEDYNAMIC instruction cannot be instrumented", new Object[]{this.context.getClassName().replaceAll("/", ".")}));
                super.visitMethodInsn(opcode, owner, name, desc, isInterface);
            } else {
                if (!this.tryReplaceCallSite(opcode, owner, name, desc) && !this.tryWrapReturnValue(opcode, owner, name, desc)) {
                    super.visitMethodInsn(opcode, owner, name, desc, isInterface);
                }

            }
        }

        public void visitTypeInsn(int opcode, String type) {
            if (opcode == Opcodes.NEW) {
                this.newInstructionFound = true;
                this.dupInstructionFound = false;
            }

            super.visitTypeInsn(opcode, type);
        }

        public void visitInsn(int opcode) {
            if (opcode == Opcodes.POP) {
                this.dupInstructionFound = true;
            }

            super.visitInsn(opcode);
        }

        private boolean tryWrapReturnValue(int opcode, String owner, String name, String desc) {
            ClassMethod method = new ClassMethod(owner, name, desc);
            ClassMethod wrappingMethod = this.context.getMethodWrapper(method);
            if (wrappingMethod != null) {
                this.log.debug(MessageFormat.format("[{0}] wrapping call to {1} with {2}", this.context.getClassName().replaceAll("/", "."), method.toString(), wrappingMethod.toString()));
                super.visitMethodInsn(opcode, owner, name, desc, opcode == Opcodes.INVOKEINTERFACE /*185*/ );
                super.visitMethodInsn(Opcodes.INVOKESTATIC/*184*/, wrappingMethod.getClassName(), wrappingMethod.getMethodName(), wrappingMethod.getMethodDesc(), false);
                this.context.markModified();
                return true;
            } else {
                return false;
            }
        }

        private boolean tryReplaceCallSite(int opcode, String owner, String name, String desc) {
            Collection replacementMethods = this.context.getCallSiteReplacements(owner, name, desc);
            if (replacementMethods.isEmpty()) {
                return false;
            } else {
                ClassMethod method = new ClassMethod(owner, name, desc);
                Iterator var7 = replacementMethods.iterator();
                if (!var7.hasNext()) {
                    return false;
                } else {
                    ClassMethod replacementMethod = (ClassMethod) var7.next();
                    boolean isSuperCallInOverride = opcode == Opcodes.INVOKESPECIAL/*183*/ && !owner.equals(this.context.getClassName()) && this.name.equals(name) && this.desc.equals(desc);
                    if (isSuperCallInOverride) {
                        this.log.debug(MessageFormat.format("[{0}] skipping call site replacement for super call in overriden method: {1}:{2}", new Object[]{this.context.getClassName().replaceAll("/", "."), this.name, this.desc}));
                        return false;
                    } else {
                        Method newMethod;
                        int[] locals;
                        int isInstanceOfLabel;
                        int local;
                        if (opcode == Opcodes.INVOKESPECIAL/*183*/ && name.equals("<init>")) {
                            newMethod = new Method(name, desc);
                            if (this.context.getSuperClassName() != null && this.context.getSuperClassName().equals(owner)) {
                                this.log.debug(MessageFormat.format("[{0}] skipping call site replacement for class extending {1}", new Object[]{this.context.getFriendlyClassName(), this.context.getFriendlySuperClassName()}));
                                return false;
                            }

                            this.log.debug(MessageFormat.format("[{0}] tracing constructor call to {1} - {2}", new Object[]{this.context.getFriendlyClassName(), method.toString(), owner}));
                            int[] var19 = new int[newMethod.getArgumentTypes().length];

                            for (int var20 = var19.length - 1; var20 >= 0; --var20) {
                                var19[var20] = this.newLocal(newMethod.getArgumentTypes()[var20]);
                                this.storeLocal(var19[var20]);
                            }

                            this.visitInsn(Opcodes.POP/*87*/);
                            if (this.newInstructionFound && this.dupInstructionFound) {
                                this.visitInsn(Opcodes.POP/*87*/);
                            }

                            locals = var19;
                            isInstanceOfLabel = var19.length;

                            for (int var23 = 0; var23 < isInstanceOfLabel; ++var23) {
                                local = locals[var23];
                                this.loadLocal(local);
                            }

                            super.visitMethodInsn(Opcodes.INVOKESTATIC/*184*/, replacementMethod.getClassName(), replacementMethod.getMethodName(), replacementMethod.getMethodDesc(), false);
                            if (this.newInstructionFound && !this.dupInstructionFound) {
                                this.visitInsn(Opcodes.POP/*87*/);
                            }
                        } else if (opcode == Opcodes.INVOKESTATIC/*184*/) {
                            this.log.debug(MessageFormat.format("[{0}] replacing static call to {1} with {2}", new Object[]{this.context.getClassName().replaceAll("/", "."), method.toString(), replacementMethod.toString()}));
                            super.visitMethodInsn(Opcodes.INVOKESTATIC/*184*/, replacementMethod.getClassName(), replacementMethod.getMethodName(), replacementMethod.getMethodDesc(), false);
                        } else {
                            newMethod = new Method(replacementMethod.getMethodName(), replacementMethod.getMethodDesc());
                            this.log.debug(MessageFormat.format("[{0}] replacing call to {1} with {2} (with instance check)", new Object[]{this.context.getClassName().replaceAll("/", "."), method.toString(), replacementMethod.toString()}));
                            Method originalMethod = new Method(name, desc);
                            locals = new int[originalMethod.getArgumentTypes().length];

                            for (isInstanceOfLabel = locals.length - 1; isInstanceOfLabel >= 0; --isInstanceOfLabel) {
                                locals[isInstanceOfLabel] = this.newLocal(originalMethod.getArgumentTypes()[isInstanceOfLabel]);
                                this.storeLocal(locals[isInstanceOfLabel]);
                            }

                            this.dup();
                            this.instanceOf(newMethod.getArgumentTypes()[0]);
                            Label var21 = new Label();
                            this.visitJumpInsn(Opcodes.IFNE/*154*/, var21);
                            int[] end = locals;
                            local = locals.length;

                            int var16;
                            int local1;
                            for (var16 = 0; var16 < local; ++var16) {
                                local1 = end[var16];
                                this.loadLocal(local1);
                            }

                            super.visitMethodInsn(opcode, owner, name, desc, opcode == 185);
                            Label var22 = new Label();
                            this.visitJumpInsn(Opcodes.GOTO/*167*/, var22);
                            this.visitLabel(var21);
                            this.checkCast(newMethod.getArgumentTypes()[0]);
                            int[] var24 = locals;
                            var16 = locals.length;

                            for (local1 = 0; local1 < var16; ++local1) {
                                int local2 = var24[local1];
                                this.loadLocal(local2);
                            }

                            super.visitMethodInsn(Opcodes.INVOKESTATIC/*184*/, replacementMethod.getClassName(), replacementMethod.getMethodName(), replacementMethod.getMethodDesc(), false);
                            this.visitLabel(var22);
                        }

                        this.context.markModified();
                        return true;
                    }
                }
            }
        }
    }
}
