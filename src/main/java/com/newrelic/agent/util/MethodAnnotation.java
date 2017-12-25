//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.newrelic.agent.util;

import java.util.Map;

public interface MethodAnnotation {
  String getMethodName();

  String getMethodDesc();

  String getClassName();

  String getName();

  Map<String, Object> getAttributes();
}
