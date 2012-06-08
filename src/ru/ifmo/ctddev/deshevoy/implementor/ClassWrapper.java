package ru.ifmo.ctddev.deshevoy.implementor;

import java.util.Map;
import java.util.Objects;

public class ClassWrapper extends TypeWrapper {
    protected Class<?> c;
    protected boolean varArgs;
    
    public ClassWrapper(Class<?> c, boolean varArgs) {
        wrappedTypes.put(c, this);
        this.c = c;
        this.varArgs = varArgs;
    }

    public void setWrappedClass(Class<?> c) {
        this.c = c;
    }
    
    public void setVarArgs(boolean varArgs) {
        this.varArgs = varArgs;
    }
    
    public Class<?> getWrappedClass() {
        return c;
    }
    
    public boolean isVarArgs() {
        return varArgs;
    }
    
    @Override
    public ClassWrapper updateTypeVariables(Map<String, TypeWrapper> typeVariables) {
        return this;
    }
    
    @Override
    public ClassWrapper getErased() {
        return this;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ClassWrapper) {
            ClassWrapper classWrapper = (ClassWrapper)obj;
            return c.equals(classWrapper.c);
        }
        return false;
    }

    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + Objects.hashCode(this.c);
        return hash;
    }
    
    @Override
    public String toString() {
        if (c.isArray()) {
            Class<?> componentType = c;
            int count = 0;
            while (componentType.isArray()) {
                count++;
                componentType = componentType.getComponentType();
            }
            String result = componentType.isMemberClass() ? componentType.getSimpleName() : componentType.getCanonicalName();
            for (int i = 0; i < count - 1; i++) {
                result += "[]";
            }
            result += varArgs ? "..." : "[]";
            return result;
        } else {
            return c.isMemberClass() ? c.getSimpleName() : c.getCanonicalName();
        }
    }
}
