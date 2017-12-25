//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.newrelic.agent.compile.transformers;

import java.lang.instrument.ClassFileTransformer;

public interface NewRelicClassTransformer extends ClassFileTransformer {
  String DEXER_CLASS_NAME = "com/android/dx/command/dexer/Main";
  String DEXER_METHOD_NAME = "processClass";
  String ANT_DEX_CLASS_NAME = "com/android/ant/DexExecTask";
  String ANT_DEX_METHOD_NAME = "preDexLibraries";
  String MAVEN_DEX_CLASS_NAME = "com/jayway/maven/plugins/android/phase08preparepackage/DexMojo";
  String PROCESS_BUILDER_CLASS_NAME = "java/lang/ProcessBuilder";
  String PROCESS_BUILDER_METHOD_NAME = "start";
  String NR_CLASS_REWRITER_CLASS_NAME = "com/newrelic/agent/compile/ClassTransformer";
  String NR_CLASS_REWRITER_METHOD_NAME = "transformClassBytes";
  String NR_CLASS_REWRITER_METHOD_SIG = "(Ljava/lang/String;[B)[B";
  String ANDROID_PACKAGE_NAME = "android/";
  String NR_PACKAGE_NAME = "com/newrelic/agent/android";

  boolean modifies(Class<?> var1);
}
