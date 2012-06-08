package ru.ifmo.ctddev.deshevoy.implementor;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.HashMap;
import java.util.Map;

public abstract class TypeWrapper {
    protected static Map<Type, TypeWrapper> wrappedTypes = new HashMap<>();
    
    public static TypeWrapper getWrappedType(Type type, boolean varArgs) {
        if (type == null) {
            return null;
        }
        if (wrappedTypes.containsKey(type)) {
            return wrappedTypes.get(type);
        }
        TypeWrapper wrappedType = null;
        if (type instanceof Class<?>) {
            wrappedType = new ClassWrapper((Class<?>)type, varArgs);
        }
        if (type instanceof GenericArrayType) {
            wrappedType = new GenericArrayTypeWrapper((GenericArrayType)type, varArgs);
        }
        if (type instanceof TypeVariable) {
            wrappedType = new TypeVariableWrapper((TypeVariable)type);
        }
        if (type instanceof ParameterizedType) {
            wrappedType = new ParameterizedTypeWrapper((ParameterizedType)type);
        }
        if (type instanceof WildcardType) {
            wrappedType = new WildcardTypeWrapper((WildcardType)type);
        }
        if (wrappedType != null) {
            return wrappedType;
        }
        throw new AssertionError("Unreachable code");
    }
    
    public static TypeWrapper getWrappedType(Type type) {
        return getWrappedType(type, false);
    }
    
    public abstract TypeWrapper updateTypeVariables(Map<String, TypeWrapper> typeVariables);
    
    public abstract ClassWrapper getErased();
    
    public abstract boolean equals(Object obj);
    
    public abstract String toString();

    public abstract int hashCode();
    
}
