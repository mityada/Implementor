package ru.ifmo.ctddev.deshevoy.implementor;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Map;

public class MethodWrapper {
    protected String name;
    protected int modifiers;
    protected TypeVariableWrapper[] typeParameters;
    protected TypeWrapper returnType;
    protected TypeWrapper[] parameterTypes;
    protected TypeWrapper[] exceptionTypes;
    
    public MethodWrapper(Method method) {
        name = method.getName();
        modifiers = method.getModifiers();
        typeParameters = new TypeVariableWrapper[method.getTypeParameters().length];
        for (int i = 0; i < method.getTypeParameters().length; i++) {
            TypeVariable<Method> typeParameter = method.getTypeParameters()[i];
            typeParameters[i] = (TypeVariableWrapper)TypeWrapper.getWrappedType(typeParameter);
        }
        returnType = TypeWrapper.getWrappedType(method.getGenericReturnType());
        parameterTypes = new TypeWrapper[method.getGenericParameterTypes().length];
        for (int i = 0; i < method.getGenericParameterTypes().length; i++) {
            Type parameterType = method.getGenericParameterTypes()[i];
            parameterTypes[i] = TypeWrapper.getWrappedType(parameterType);
        }
        exceptionTypes = new TypeWrapper[method.getGenericExceptionTypes().length];
        for (int i = 0; i < method.getGenericExceptionTypes().length; i++) {
            Type exceptionType = method.getGenericExceptionTypes()[i];
            exceptionTypes[i] = TypeWrapper.getWrappedType(exceptionType);
        }
    }
    
    private MethodWrapper(String name, int modifiers, TypeVariableWrapper[] typeParameters, TypeWrapper returnType, TypeWrapper[] parameterTypes, TypeWrapper[] exceptionTypes) {
        this.name = name;
        this.modifiers = modifiers;
        this.typeParameters = typeParameters;
        this.returnType = returnType;
        this.parameterTypes = parameterTypes;
        this.exceptionTypes = exceptionTypes;
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
    
    public void setReturnType(TypeWrapper returnType) {
        this.returnType = returnType;
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
    
    public TypeWrapper getReturnType() {
        return returnType;
    }
    
    public TypeWrapper[] getParameterTypes() {
        return parameterTypes;
    }
    
    public TypeWrapper[] getExceptionTypes() {
        return exceptionTypes;
    }
    
    public MethodSignature getSignature() {
        return new MethodSignature(name, parameterTypes);
    }
    
    public MethodWrapper getErased() {
        TypeVariableWrapper[] erasedTypeParameters = new TypeVariableWrapper[0];
        TypeWrapper erasedReturnType = this.returnType.getErased();
        TypeWrapper[] erasedParameterTypes = new TypeWrapper[this.parameterTypes.length];
        for (int i = 0; i < this.parameterTypes.length; i++) {
            erasedParameterTypes[i] = this.parameterTypes[i].getErased();
        }
        TypeWrapper[] erasedExceptionTypes = new TypeWrapper[this.exceptionTypes.length];
        for (int i = 0; i < this.exceptionTypes.length; i++) {
            erasedExceptionTypes[i] = this.exceptionTypes[i].getErased();
        }
        return new MethodWrapper(name, modifiers, erasedTypeParameters, erasedReturnType, erasedParameterTypes, erasedExceptionTypes);
    }
    
    public MethodWrapper updateTypeVariables(Map<String, TypeWrapper> typeVariables) {
        TypeVariableWrapper[] updatedTypeParameters = new TypeVariableWrapper[typeParameters.length];
        for (int i = 0; i < typeParameters.length; i++) {
            updatedTypeParameters[i] = (TypeVariableWrapper)typeParameters[i].updateTypeVariables(typeVariables);
        }
        TypeWrapper updatedReturnType = this.returnType.updateTypeVariables(typeVariables);
        TypeWrapper[] updatedParameterTypes = new TypeWrapper[this.parameterTypes.length];
        for (int i = 0; i < this.parameterTypes.length; i++) {
            updatedParameterTypes[i] = this.parameterTypes[i].updateTypeVariables(typeVariables);
        }
        TypeWrapper[] updatedExceptionTypes = new TypeWrapper[this.exceptionTypes.length];
        for (int i = 0; i < this.exceptionTypes.length; i++) {
            updatedExceptionTypes[i] = this.exceptionTypes[i].updateTypeVariables(typeVariables);
        }
        return new MethodWrapper(name, modifiers, updatedTypeParameters, updatedReturnType, updatedParameterTypes, updatedExceptionTypes);
    }
    
}
