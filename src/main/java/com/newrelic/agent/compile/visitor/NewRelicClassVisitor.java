//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.newrelic.agent.compile.visitor;

import com.newrelic.agent.Obfuscation.Proguard;
import com.newrelic.agent.compile.InstrumentationContext;
import com.newrelic.agent.compile.Log;
import com.newrelic.agent.compile.RewriterAgent;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.GeneratorAdapter;

import java.util.UUID;

public class NewRelicClassVisitor extends ClassVisitor {
    public static final String BUILD_ID_KEY = "NewRelic.BuildId";
    private static String buildId;
    private final InstrumentationContext context;
    private final Log log;

    public NewRelicClassVisitor(ClassVisitor cv, InstrumentationContext context, Log log) {
        super(Opcodes.ASM5, cv);
        this.context = context;
        this.log = log;
    }

    public static String getBuildId() {
        if (buildId == null) {
            buildId = UUID.randomUUID().toString();
            System.setProperty("NewRelic.BuildId", buildId);
        }

        return buildId;
    }

    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        return (this.context.getClassName().equals("com/newrelic/agent/android/NewRelic") && name.equals("isInstrumented") ? new NewRelicClassVisitor.NewRelicMethodVisitor(super.visitMethod(access, name, desc, signature, exceptions), access, name, desc) : (this.context.getClassName().equals("com/newrelic/agent/android/crash/Crash") && name.equals("getBuildId") ? new NewRelicClassVisitor.BuildIdMethodVisitor(super.visitMethod(access, name, desc, signature, exceptions), access, name, desc) : (this.context.getClassName().equals("com/newrelic/agent/android/AndroidAgentImpl") && name.equals("pokeCanary") ? new NewRelicClassVisitor.CanaryMethodVisitor(super.visitMethod(access, name, desc, signature, exceptions), access, name, desc) : super.visitMethod(access, name, desc, signature, exceptions))));
    }

    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        if (this.context.getClassName().equals("com/newrelic/agent/android/Agent") && name.equals("VERSION") && !value.equals(RewriterAgent.getVersion())) {
            this.log.warning("New Relic Error: Your agent and class rewriter versions do not match: agent[" + value + "] class rewriter[" + RewriterAgent.getVersion() + "]. " + "You may need to update one of these components, or simply invalidate your AndroidStudio cache.  " + "If you\'re using gradle and just updated, run gradle -stop to restart the daemon.");
        }

        return super.visitField(access, name, desc, signature, value);
    }

    private final class CanaryMethodVisitor extends GeneratorAdapter {
        private boolean foundCanaryAlive = false;

        public CanaryMethodVisitor(MethodVisitor mv, int access, String name, String desc) {
            super(Opcodes.ASM5, mv, access, name, desc);
        }

        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean b) {
            if (name.equals("canaryMethod")) {
                this.foundCanaryAlive = true;
            }

        }

        public void visitEnd() {
            if (this.foundCanaryAlive) {
                NewRelicClassVisitor.this.log.info("Found canary alive");
            } else {
                NewRelicClassVisitor.this.log.info("Evidence of Proguard/Dexguard detected, sending mapping.txt");
                Proguard proguard = new Proguard(NewRelicClassVisitor.this.log);
                proguard.findAndSendMapFile();
            }

        }
    }

    private final class NewRelicMethodVisitor extends GeneratorAdapter {
        public NewRelicMethodVisitor(MethodVisitor mv, int access, String name, String desc) {
            super(Opcodes.ASM5, mv, access, name, desc);
        }

        public void visitCode() {
            super.visitInsn(Opcodes.ACC_PROTECTED/*4*/);
            super.visitInsn(Opcodes.IRETURN/*172*/);
            NewRelicClassVisitor.this.log.info("[NewRelicMethodVisitor] Marking NewRelic agent as instrumented");
            NewRelicClassVisitor.this.context.markModified();
        }
    }

    private final class BuildIdMethodVisitor extends GeneratorAdapter {
        public BuildIdMethodVisitor(MethodVisitor mv, int access, String name, String desc) {
            super(Opcodes.ASM5, mv, access, name, desc);
        }

        public void visitCode() {
            super.visitLdcInsn(NewRelicClassVisitor.getBuildId());
            super.visitInsn(Opcodes.ARETURN/*176*/);
            NewRelicClassVisitor.this.log.info("[NewRelicMethodVisitor] Setting build identifier to [" + NewRelicClassVisitor.getBuildId() + "]");
            NewRelicClassVisitor.this.context.markModified();
        }
    }
}
