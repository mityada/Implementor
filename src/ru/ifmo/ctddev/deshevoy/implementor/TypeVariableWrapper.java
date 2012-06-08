package ru.ifmo.ctddev.deshevoy.implementor;

import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.TypeVariable;
import java.util.Map;
import java.util.Objects;

public class TypeVariableWrapper extends TypeWrapper {
    protected String name;
    protected TypeWrapper bounds;
    protected GenericDeclaration declaration;
    
    public TypeVariableWrapper(TypeVariable typeVariable) {
        wrappedTypes.put(typeVariable, this);
        name = typeVariable.getName();
        bounds = TypeWrapper.getWrappedType(typeVariable.getBounds()[0]);
        declaration = typeVariable.getGenericDeclaration();
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public void setBounds(TypeWrapper bounds) {
        this.bounds = bounds;
    }
    
    public void setDeclaration(GenericDeclaration declaration) {
        this.declaration = declaration;
    }
    
    public String getName() {
        return name;
    }
    
    public TypeWrapper getBounds() {
        return bounds;
    }
    
    public GenericDeclaration getDeclaration() {
        return declaration;
    }
    
    @Override
    public TypeWrapper updateTypeVariables(Map<String, TypeWrapper> typeVariables) {
        if (typeVariables.containsKey(name)) {
            return typeVariables.get(name);
        } else {
            return this;
        }
    }
    
    @Override
    public ClassWrapper getErased() {
        return bounds.getErased();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TypeVariableWrapper) {
            TypeVariableWrapper typeVariableWrapper = (TypeVariableWrapper)obj;
            return name.equals(typeVariableWrapper.name);
        }
        return false;
    }

    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.name);
        return hash;
    }
    
    public String toString(boolean bounds) {
        String result = name;
        if (bounds && this.bounds != null && !this.bounds.getErased().getWrappedClass().equals(Object.class)) {
            result += " extends " + this.bounds.toString();
        }
        return result;
    }
    
    public String toString() {
        return toString(false);
    }
}
