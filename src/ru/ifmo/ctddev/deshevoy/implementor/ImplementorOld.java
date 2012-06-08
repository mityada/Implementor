package ru.ifmo.ctddev.deshevoy.implementor;

import java.io.Writer;
import java.io.FileWriter;
import java.io.IOException;

import java.lang.reflect.Constructor;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;

public class ImplementorOld {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java -jar Implementor.jar <classname>");
            System.exit(0);
        }
        
        Class c = null;
        try {
            c = Class.forName(args[0]);
        } catch (ClassNotFoundException e) {
            System.out.println("Class not found: " + e.getMessage());
            System.exit(0);
        }
        
        if (c.isEnum()) {
            System.out.println(c.getCanonicalName() + " is enum");
            System.exit(0);
        }
        
        int modifiers = c.getModifiers();
        if (Modifier.isFinal(modifiers)) {
            System.out.println(c.getCanonicalName() + " is final");
            System.exit(0);
        }
        
        Writer writer = null;
        try {            
            writer = new FileWriter("../ImplementorTest/src/" + c.getSimpleName() + "Impl.java");
            
            generateClass(writer, c);
            
            writer.close();
        } catch (IOException e) {
            try {
                writer.close();
            } catch (IOException e2) {
                
            }
        }
    }
    
    public static void generateClass(Writer writer, Class c) throws IOException {
        writer.write("public class " + c.getSimpleName() + "Impl");

        writer.write(getTypeParametersString(c.getTypeParameters(), true, null, false));

        writer.write(" ");
        
        if (c.isInterface()) {
            writer.write("implements");
        } else {
            writer.write("extends");
        }

        writer.write(" " + c.getName() + getTypeParametersString(c.getTypeParameters(), false, null, false) + " {\n");

        for (Constructor<?> constructor : c.getConstructors()) {
            generateConstructor(writer, constructor, c.getSimpleName() + "Impl");
        }
        
        Map<String, Type> typeVariables = new HashMap<>();
        
        Map<String, Map<Type[], Method>> addedMethods = new HashMap<>();
        Map<Method, Map<String, Type>> methodTypeVariables = new HashMap<>();
        Map<Method, Boolean> methodErasure = new HashMap<>();
        
        addAbstractMethods(c, c, addedMethods, methodTypeVariables, typeVariables, methodErasure);
        
        for (Map.Entry<String, Map<Type[], Method>> namesEntry : addedMethods.entrySet()) {
            for (Map.Entry<Type[], Method> parametersEntry : namesEntry.getValue().entrySet()) {
                generateMethod(writer, parametersEntry.getValue(), methodTypeVariables.get(parametersEntry.getValue()), (methodErasure.containsKey(parametersEntry.getValue()) ? methodErasure.get(parametersEntry.getValue()) : false));
            }
        }
        
        writer.write("}\n");
    }
    
    public static void addAbstractMethods(Type t, Type baseType, Map<String, Map<Type[], Method>> methods, Map<Method, Map<String, Type>> methodTypeVariables, Map<String, Type> typeVariables, Map<Method, Boolean> methodErasure) {
        if (t instanceof Class<?>) {
            Class<?> c = (Class<?>)t;
            
            boolean erase = false;
            
            for (TypeVariable typeVariable : c.getTypeParameters()) {
                if (!typeVariables.containsKey(typeVariable.getName()) && !t.equals(baseType)) {
                    erase = true;
                }
            }
            
            for (Method method : c.getDeclaredMethods()) {
                if (Modifier.isAbstract(method.getModifiers())) {
                    addMethod(method, methods, methodTypeVariables, typeVariables, methodErasure, erase);
                }
            }
            
            if (c.getGenericSuperclass() != null) {
                addAbstractMethods(c.getGenericSuperclass(), baseType, methods, methodTypeVariables, typeVariables, methodErasure);
            }
            
            for (Type i : c.getGenericInterfaces()) {
                addAbstractMethods(i, baseType, methods, methodTypeVariables, typeVariables, methodErasure);
            }
            
            return;
        }
        if (t instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType)t;
            addAbstractMethods(parameterizedType.getRawType(), baseType, methods, methodTypeVariables, updateTypeVaribles(parameterizedType, typeVariables), methodErasure);
            return;
        }
    }
    
    public static void addMethod(Method method, Map<String, Map<Type[], Method>> methods, Map<Method, Map<String, Type>> methodTypeVariables, Map<String, Type> typeVariables, Map<Method, Boolean> methodErasure, boolean erase) {
        if (methods.containsKey(method.getName())) {
            Map<Type[], Method> methodsMap = methods.get(method.getName());
            
            Boolean contains = false;
            for (Map.Entry<Type[], Method> entry : methodsMap.entrySet()) {
                if (Arrays.equals(method.getGenericParameterTypes(), entry.getKey())) {
                    if (entry.getValue().getReturnType().isAssignableFrom(method.getReturnType())) {
                        //methodsMap.remove(entry.getKey());
                        methodsMap.put(method.getGenericParameterTypes(), method);
                        //methodTypeVariables.remove(entry.getValue());
                        methodTypeVariables.put(method, typeVariables);
                        //methodErasure.remove(entry.getValue());
                        methodErasure.put(method, erase);
                    }
                
                    contains = true;
                    break;
                }
            }
            
            if (!contains) {
                methodsMap.put(method.getGenericParameterTypes(), method);
            }
        } else {
            methods.put(method.getName(), new HashMap<Type[], Method>());
            methods.get(method.getName()).put(method.getGenericParameterTypes(), method);
            methodTypeVariables.put(method, typeVariables);
            methodErasure.put(method, erase);
        }
    }
    
    public static Map<String, Type> updateTypeVaribles(ParameterizedType parameterizedType, Map<String, Type> typeVariables) {
        Class<?> rawType = (Class<?>)parameterizedType.getRawType();
        Map<String, Type> updatedTypeVariables = new HashMap<>(typeVariables);
        TypeVariable[] typeParameters = rawType.getTypeParameters();
        for (int i = 0; i < typeParameters.length; i++) {
            Type typeParameter = parameterizedType.getActualTypeArguments()[i];
            if (typeParameter instanceof TypeVariable) {
                TypeVariable typeVariable = (TypeVariable)typeParameter;
                if (typeVariables.containsKey(typeVariable.getName())) {
                    typeParameter = typeVariables.get(typeVariable.getName());
                }
            }
            if (!(typeParameter instanceof TypeVariable) || !((TypeVariable)typeParameter).getName().equals(typeParameters[i].getName())) {
                updatedTypeVariables.put(typeParameters[i].getName(), typeParameter);
            }
        }
        return updatedTypeVariables;
    }
    
    public static void generateConstructor(Writer writer, Constructor<?> constructor, String className) throws IOException {
        writer.write("\t" + getModifiersString(constructor.getModifiers(), ~0) + " ");
        
        String typeParameters = getTypeParametersString(constructor.getTypeParameters(), true, null, false);
        if (typeParameters.length() > 0) {
            typeParameters += " ";
        }
        
        writer.write(className + " (");
        
        Type[] parameters = constructor.getGenericParameterTypes();
        for (int j = 0; j < parameters.length; j++) {
            writer.write(getTypeString(parameters[j], constructor.isVarArgs() & j == parameters.length - 1, null, false) + " arg" + j);
            if (j != parameters.length - 1) {
                writer.write(", ");
            }
        }
        writer.write(") ");
                    
        if (constructor.getExceptionTypes().length > 0) {
            writer.write("throws ");
            for (int j = 0; j < constructor.getGenericExceptionTypes().length; j++) {
                writer.write(getTypeString(constructor.getGenericExceptionTypes()[j], false, null, false));
                if (j != constructor.getExceptionTypes().length - 1) {
                    writer.write(", ");
                }
            }
            writer.write(" ");
        }
                    
        writer.write("{\n");
        writer.write("\t\tsuper(");

        for (int j = 0; j < parameters.length; j++) {
            writer.write("arg" + j);
            if (j != parameters.length - 1) {
                writer.write(", ");
            }
        }

        writer.write(");\n");
        writer.write("\t}\n\n");
    }
    
    public static void generateMethod(Writer writer, Method method, Map<String, Type> typeVariables, boolean erase) throws IOException {
        writer.write("\t@Override\n");
        writer.write("\t" + getModifiersString(method.getModifiers(), ~(Modifier.ABSTRACT | Modifier.TRANSIENT)) + " ");

        String typeParameters = getTypeParametersString(method.getTypeParameters(), true, typeVariables, erase);
        if (typeParameters.length() > 0) {
           typeParameters += " ";
        }
        writer.write(typeParameters);

        Type returnType = method.getGenericReturnType();
        writer.write(getTypeString(returnType, false, typeVariables, erase));

        writer.write(" ");
        writer.write(method.getName() + "(");
        Type[] parameters = method.getGenericParameterTypes();
        for (int j = 0; j < parameters.length; j++) {
            writer.write(getTypeString(parameters[j], method.isVarArgs() & j == parameters.length - 1, typeVariables, erase) + " arg" + j);
            if (j != parameters.length - 1) {
                writer.write(", ");
            }
        }
        writer.write(") ");
                    
        if (method.getExceptionTypes().length > 0) {
            writer.write("throws ");
            for (int j = 0; j < method.getGenericExceptionTypes().length; j++) {
                writer.write(getTypeString(method.getGenericExceptionTypes()[j], false, typeVariables, erase));
                if (j != method.getExceptionTypes().length - 1) {
                    writer.write(", ");
                }
            }
            writer.write(" ");
        }
                    
        writer.write("{\n");
        writer.write("\t\treturn");

        if (!method.getReturnType().equals(Void.TYPE)) {
            if (method.getReturnType().equals(Boolean.TYPE)) {
                writer.write(" false");
            } else if (method.getReturnType().isPrimitive()) {
                writer.write(" 0");
            } else {
                writer.write(" null");
            }
        }

        writer.write(";\n");
        writer.write("\t}\n\n");
    }
    
    public static String getModifiersString(int modifiers, int mask) {
        return Modifier.toString(modifiers & mask);
    }
    
    public static String getTypeString(Type type, boolean varargs, Map<String, Type> typeVariables, boolean erase) {
        if (type instanceof Class) {
            Class c = (Class)type;
            if (c.isArray()) {
                return getTypeString(c.getComponentType(), false, typeVariables, erase) + (varargs ? "..." : "[]");
            } else {
                if (!c.isMemberClass()) {
                    return c.getCanonicalName();
                } else {
                    return c.getSimpleName();
                }
            }
        }
        if (type instanceof TypeVariable) {
            TypeVariable typeVariable = (TypeVariable)type;
            if (erase) {
                return getTypeString(typeVariable.getBounds()[0], false, typeVariables, erase);
            } else {
                if (typeVariables != null && typeVariables.containsKey(typeVariable.getName())) {
                    return getTypeString(typeVariables.get(typeVariable.getName()), false, typeVariables, erase);
                } else {
                    return typeVariable.getName();
                }
            }
        }
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType)type;
            String result = "";
            if (parameterizedType.getOwnerType() != null) {
                result += getTypeString(parameterizedType.getOwnerType(), false, typeVariables, erase) + ".";
            }
            result += getTypeString(parameterizedType.getRawType(), false, typeVariables, erase);
            if (!erase) {
                result += "<";
                for (int i = 0; i < parameterizedType.getActualTypeArguments().length; i++) {
                    result += getTypeString(parameterizedType.getActualTypeArguments()[i], false, typeVariables, erase);
                    if (i != parameterizedType.getActualTypeArguments().length - 1) {
                        result += ", ";
                    }
                }
                result += ">";
            }
            return result;
        }
        if (type instanceof GenericArrayType) {
            return getTypeString(((GenericArrayType)type).getGenericComponentType(), false, typeVariables, erase) + (varargs ? "..." : "[]");
        }
        if (type instanceof WildcardType) {
            WildcardType wildcardType = (WildcardType)type;
            String result = "?";
            if (wildcardType.getUpperBounds().length != 0 && !wildcardType.getUpperBounds()[0].equals(Object.class)) {
                result += " extends " + getTypeString(wildcardType.getUpperBounds()[0], false, typeVariables, erase);
            }
            if (wildcardType.getLowerBounds().length != 0) {
                result += " super " + getTypeString(wildcardType.getLowerBounds()[0], false, typeVariables, erase);
            }
            return result;
        }
        return "";
    }
    
    public static String getTypeParametersString(TypeVariable<?>[] parameters, boolean bounds, Map<String, Type> typeVariables, boolean erase) {
        String result = "";
        if (parameters.length > 0) {
            result += "<";
            for (int i = 0; i < parameters.length; i++) {
                result += parameters[i].getName();
                if (bounds && parameters[i].getBounds().length != 0) {
                    result += " extends " + getTypeString(parameters[i].getBounds()[0], false, typeVariables, erase);
                }
                if (i != parameters.length - 1) {
                    result += ", ";
                }
            }
            result += ">";
        }
        return result;
    }
}
