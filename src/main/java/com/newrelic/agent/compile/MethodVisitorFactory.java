//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.newrelic.agent.compile;

import org.objectweb.asm.MethodVisitor;

public interface MethodVisitorFactory {
  MethodVisitor create(MethodVisitor var1, int var2, String var3, String var4);
}
