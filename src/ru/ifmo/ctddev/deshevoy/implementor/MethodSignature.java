package ru.ifmo.ctddev.deshevoy.implementor;

import java.util.Arrays;
import java.util.Objects;

public class MethodSignature {
    protected String name;
    protected TypeWrapper[] parameterTypes;
    
    public MethodSignature(String name, TypeWrapper[] parameterTypes) {
        this.name = name;
        this.parameterTypes = parameterTypes;
    }

    public int hashCode() {
        int hash = 7;
        hash = 19 * hash + Objects.hashCode(this.name);
        hash = 19 * hash + Arrays.deepHashCode(this.parameterTypes);
        return hash;
    }
    
    public boolean equals(Object obj) {
        if (obj instanceof MethodSignature) {
            MethodSignature methodSignature = (MethodSignature)obj;
            if (!name.equals(methodSignature.name)) {
                return false;
            }
            return Arrays.equals(parameterTypes, methodSignature.parameterTypes);
        }
        return false;
    }
}
