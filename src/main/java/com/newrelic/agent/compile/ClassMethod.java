//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.newrelic.agent.compile;

import org.objectweb.asm.commons.Method;

public final class ClassMethod {
    private final String className;
    private final String methodName;
    private final String methodDesc;

    public ClassMethod(String className, String methodName, String methodDesc) {
        this.className = className;
        this.methodName = methodName;
        this.methodDesc = methodDesc;
    }

    static ClassMethod getClassMethod(String signature) {
        try {
            int descIndex = signature.lastIndexOf('(');
            String methodDesc;
            if(descIndex == -1) {
                descIndex = signature.length();
                methodDesc = "";
            } else {
                methodDesc = signature.substring(descIndex);
            }

            String beforeMethodDesc = signature.substring(0, descIndex);
            int methodIndex = beforeMethodDesc.lastIndexOf('.');
            return new ClassMethod(signature.substring(0, methodIndex), signature.substring(methodIndex + 1, descIndex), methodDesc);
        } catch (Exception var5) {
            throw new RuntimeException("Error parsing " + signature, var5);
        }
    }

    Method getMethod() {
        return new Method(this.methodName, this.methodDesc);
    }

    public String getClassName() {
        return this.className;
    }

    public String getMethodName() {
        return this.methodName;
    }

    public String getMethodDesc() {
        return this.methodDesc;
    }

    public int hashCode() {
        int prime = 31;
        int result = 1;

        result = 31 * result + (this.className == null ? 0 : this.className.hashCode());

        result = 31 * result + (this.methodDesc == null ? 0 : this.methodDesc.hashCode());

        result = 31 * result + (this.methodName == null ? 0 : this.methodName.hashCode());
        return result;
    }

    public boolean equals(Object obj) {
        if(this == obj) {
            return true;
        } else if(obj == null) {
            return false;
        } else if(this.getClass() != obj.getClass()) {
            return false;
        } else {
            ClassMethod other = (ClassMethod)obj;
            if(this.className == null) {
                if(other.className != null) {
                    return false;
                }
            } else if(!this.className.equals(other.className)) {
                return false;
            }

            if(this.methodDesc == null) {
                if(other.methodDesc != null) {
                    return false;
                }
            } else if(!this.methodDesc.equals(other.methodDesc)) {
                return false;
            }

            if(this.methodName == null) {
                if(other.methodName != null) {
                    return false;
                }
            } else if(!this.methodName.equals(other.methodName)) {
                return false;
            }

            return true;
        }
    }

    public String toString() {
        return this.className + '.' + this.methodName + this.methodDesc;
    }
}
