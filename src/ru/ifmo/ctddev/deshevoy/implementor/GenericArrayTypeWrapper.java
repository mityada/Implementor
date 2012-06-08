package ru.ifmo.ctddev.deshevoy.implementor;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.util.Map;
import java.util.Objects;

public class GenericArrayTypeWrapper extends TypeWrapper {
    protected TypeWrapper componentType;
    protected boolean varArgs;
    
    public GenericArrayTypeWrapper(GenericArrayType genericArrayType, boolean varArgs) {
        wrappedTypes.put(genericArrayType, this);
        componentType = TypeWrapper.getWrappedType(genericArrayType.getGenericComponentType());
        this.varArgs = varArgs;
    }
    
    private GenericArrayTypeWrapper(TypeWrapper componentType, boolean varArgs) {
        this.componentType = componentType;
        this.varArgs = varArgs;
    }
    
    public void setComponentType(TypeWrapper componentType) {
        this.componentType = componentType;
    }
    
    public void setVarArgs(boolean varArgs) {
        this.varArgs = varArgs;
    }
    
    public TypeWrapper getComponentType() {
        return componentType;
    }
    
    public boolean isVarArgs() {
        return varArgs;
    }
    
    @Override
    public GenericArrayTypeWrapper updateTypeVariables(Map<String, TypeWrapper> typeVariables) {
        return new GenericArrayTypeWrapper(componentType.updateTypeVariables(typeVariables), varArgs);
    }

    @Override
    public ClassWrapper getErased() {
        ClassWrapper component = componentType.getErased();
        if (component == null) {
            return null;
        }
        return (ClassWrapper)TypeWrapper.getWrappedType(Array.newInstance(component.c, 0).getClass(), varArgs);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof GenericArrayTypeWrapper) {
            GenericArrayTypeWrapper genericArrayTypeWrapper = (GenericArrayTypeWrapper)obj;
            return componentType.equals(genericArrayTypeWrapper.componentType);
        }
        return false;
    }

    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.componentType);
        return hash;
    }
    
    @Override
    public String toString() {
        return componentType.toString() + (varArgs ? "..." : "[]");
    }
}
