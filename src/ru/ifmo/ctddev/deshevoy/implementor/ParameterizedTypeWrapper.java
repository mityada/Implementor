package ru.ifmo.ctddev.deshevoy.implementor;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

public class ParameterizedTypeWrapper extends TypeWrapper {
    protected TypeWrapper ownerType;
    protected TypeWrapper rawType;
    protected TypeWrapper[] actualTypeArguments;
    
    public ParameterizedTypeWrapper(ParameterizedType parameterizedType) {
        wrappedTypes.put(parameterizedType, this);
        ownerType = TypeWrapper.getWrappedType(parameterizedType.getOwnerType());
        rawType = TypeWrapper.getWrappedType(parameterizedType.getRawType());
        Type[] args = parameterizedType.getActualTypeArguments();
        actualTypeArguments = new TypeWrapper[args.length];
        for (int i = 0; i < args.length; i++) {
            actualTypeArguments[i] = TypeWrapper.getWrappedType(args[i]);
        }
    }
    
    private ParameterizedTypeWrapper(TypeWrapper ownerType, TypeWrapper rawType, TypeWrapper[] actualTypeArguments) {
        this.ownerType = ownerType;
        this.rawType = rawType;
        this.actualTypeArguments = actualTypeArguments;
    }
    
    public void setOwnerType(TypeWrapper ownerType) {
        this.ownerType = ownerType;
    }
    
    public void setRawType(TypeWrapper rawType) {
        this.rawType = rawType;
    }
    
    public void setActualTypeArguments(TypeWrapper[] actualTypeArguments) {
        this.actualTypeArguments = actualTypeArguments;
    }
    
    public TypeWrapper getOwnerType() {
        return ownerType;
    }
    
    public TypeWrapper getRawType() {
        return rawType;
    }
    
    public TypeWrapper[] getActualTypeArguments() {
        return actualTypeArguments;
    }
    
    @Override
    public ParameterizedTypeWrapper updateTypeVariables(Map<String, TypeWrapper> typeVariables) {
        TypeWrapper updatedOwnerType = null;
        if (ownerType != null) {
            updatedOwnerType = ownerType.updateTypeVariables(typeVariables);
        }
        TypeWrapper updatedRawType = rawType.updateTypeVariables(typeVariables);
        TypeWrapper[] updatedActualTypeArguments = new TypeWrapper[actualTypeArguments.length];
        for (int i = 0; i < actualTypeArguments.length; i++) {
            updatedActualTypeArguments[i] = actualTypeArguments[i].updateTypeVariables(typeVariables);
        }
        return new ParameterizedTypeWrapper(updatedOwnerType, updatedRawType, updatedActualTypeArguments);
    }
    
    @Override
    public ClassWrapper getErased() {
        return rawType.getErased();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ParameterizedTypeWrapper) {
            ParameterizedTypeWrapper parameterizedTypeWrapper = (ParameterizedTypeWrapper)obj;
            if (ownerType == null ^ parameterizedTypeWrapper.ownerType == null || (ownerType != null && !ownerType.equals(parameterizedTypeWrapper.ownerType))) {
                return false;
            }
            return rawType.equals(parameterizedTypeWrapper.rawType) && Arrays.equals(actualTypeArguments, parameterizedTypeWrapper.actualTypeArguments);
        }
        return false;
    }

    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + Objects.hashCode(this.ownerType);
        hash = 89 * hash + Objects.hashCode(this.rawType);
        hash = 89 * hash + Arrays.deepHashCode(this.actualTypeArguments);
        return hash;
    }
    
    @Override
    public String toString() {
        String result = "";
        if (ownerType != null) {
            result += ownerType.toString() + ".";
        }
        result += rawType.toString();
        result += "<";
        for (int i = 0; i < actualTypeArguments.length; i++) {
            result += actualTypeArguments[i].toString();
            if (i != actualTypeArguments.length - 1) {
                result += ", ";
            }
        }
        result += ">";
        return result;
    }
}
