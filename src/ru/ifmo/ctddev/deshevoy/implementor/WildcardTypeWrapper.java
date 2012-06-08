package ru.ifmo.ctddev.deshevoy.implementor;

import java.lang.reflect.WildcardType;
import java.util.Map;
import java.util.Objects;

public class WildcardTypeWrapper extends TypeWrapper {
    protected TypeWrapper upperBounds;
    protected TypeWrapper lowerBounds;
    
    public WildcardTypeWrapper(WildcardType wildcardType) {
        wrappedTypes.put(wildcardType, this);
        if (wildcardType.getUpperBounds().length != 0) {
            upperBounds = TypeWrapper.getWrappedType(wildcardType.getUpperBounds()[0]);
        }
        if (wildcardType.getLowerBounds().length != 0) {
            lowerBounds = TypeWrapper.getWrappedType(wildcardType.getLowerBounds()[0]);
        }
    }
    
    private WildcardTypeWrapper(TypeWrapper upperBounds, TypeWrapper lowerBounds) {
        this.upperBounds = upperBounds;
        this.lowerBounds = lowerBounds;
    }
    
    public void setUpperBounds(TypeWrapper upperBounds) {
        this.upperBounds = upperBounds;
    }
    
    public void setLowerBounds(TypeWrapper lowerBounds) {
        this.lowerBounds = lowerBounds;
    }
    
    public TypeWrapper getUpperBounds() {
        return upperBounds;
    }
    
    public TypeWrapper getLowerBounds() {
        return lowerBounds;
    }
    
    @Override
    public WildcardTypeWrapper updateTypeVariables(Map<String, TypeWrapper> typeVariables) {
        return new WildcardTypeWrapper(upperBounds == null ? null : upperBounds.updateTypeVariables(typeVariables), lowerBounds == null ? null : lowerBounds.updateTypeVariables(typeVariables));
    }
    
    @Override
    public ClassWrapper getErased() {
        throw new AssertionError("Unreachable code");
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof WildcardTypeWrapper) {
            WildcardTypeWrapper wildcardTypeWrapper = (WildcardTypeWrapper)obj;
            if (upperBounds == null ^ wildcardTypeWrapper.upperBounds == null || (upperBounds != null && !upperBounds.equals(wildcardTypeWrapper.upperBounds))) {
                return false;
            }
            if (lowerBounds == null ^ wildcardTypeWrapper.lowerBounds == null || (lowerBounds != null && !lowerBounds.equals(wildcardTypeWrapper.lowerBounds))) {
                return false;
            }
            return true;
        }
        return false;
    }

    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Objects.hashCode(this.upperBounds);
        hash = 79 * hash + Objects.hashCode(this.lowerBounds);
        return hash;
    }

    @Override
    public String toString() {
        String result = "?";
        if (upperBounds != null && !upperBounds.equals(TypeWrapper.getWrappedType(Object.class))) {
            result += " extends " + upperBounds.toString();
        }
        if (lowerBounds != null) {
            result += " super " + lowerBounds.toString();
        }
        return result;
    }
    
}
