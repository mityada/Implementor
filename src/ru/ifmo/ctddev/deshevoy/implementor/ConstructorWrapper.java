package ru.ifmo.ctddev.deshevoy.implementor;

import java.lang.reflect.Constructor;

public class ConstructorWrapper {
    protected String name;
    protected int modifiers;
    protected TypeVariableWrapper[] typeParameters;
    protected TypeWrapper[] parameterTypes;
    protected TypeWrapper[] exceptionTypes;
    
    public ConstructorWrapper(Constructor constructor) {
        name = constructor.getName();
        modifiers = constructor.getModifiers();
        typeParameters = new TypeVariableWrapper[constructor.getTypeParameters().length];
        for (int i = 0; i < constructor.getTypeParameters().length; i++) {
            typeParameters[i] = new TypeVariableWrapper(constructor.getTypeParameters()[i]);
        }
        parameterTypes = new TypeWrapper[constructor.getGenericParameterTypes().length];
        for (int i = 0; i < constructor.getGenericParameterTypes().length; i++) {
            parameterTypes[i] = TypeWrapper.getWrappedType(constructor.getGenericParameterTypes()[i]);
        }
        exceptionTypes = new TypeWrapper[constructor.getGenericExceptionTypes().length];
        for (int i = 0; i < constructor.getGenericExceptionTypes().length; i++) {
            exceptionTypes[i] = TypeWrapper.getWrappedType(constructor.getGenericExceptionTypes()[i]);
        }
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setModifiers(int modifiers) {
        this.modifiers = modifiers;
    }
    
    public void setTypeParameters(TypeVariableWrapper[] typeParameters) {
        this.typeParameters = typeParameters;
    }
    
    public void setParameterTypes(TypeWrapper[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }
    
    public void setExceptionTypes(TypeWrapper[] exceptionTypes) {
        this.exceptionTypes = exceptionTypes;
    }
    
    public String getName() {
        return name;
    }
    
    public int getModifiers() {
        return modifiers;
    }
    
    public TypeVariableWrapper[] getTypeParameters() {
        return typeParameters;
    }
    
    public TypeWrapper[] getParameterTypes() {
        return parameterTypes;
    }
    
    public TypeWrapper[] getExceptionTypes() {
        return exceptionTypes;
    }
        
}
