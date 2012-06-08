package ru.ifmo.ctddev.deshevoy.implementor;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Map;

public class Implementor {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java ImplementorNew <classname>");
            System.exit(0);
        }
        
        Class<?> baseClass = null;
        
        try {
            baseClass = Class.forName(args[0]);
        } catch (ClassNotFoundException e) {
            System.out.println("Class not found: " + e.getMessage());
            System.exit(0);
        }
        
        if (baseClass.isEnum()) {
            System.out.println(baseClass.getCanonicalName() + " is enum");
            System.exit(0);
        }
        
        if (Modifier.isFinal(baseClass.getModifiers())) {
            System.out.println(baseClass.getCanonicalName() + " is final");
            System.exit(0);
        }
        
        PrintWriter writer = null;
        try {
            writer = new PrintWriter("../ImplementorTest/src/" + baseClass.getSimpleName() + "Impl.java");
            
            generateClass(writer, baseClass);
            
            writer.close();
        } catch (IOException e) {
            writer.close();
        }
    }
    
    public static void generateClass(PrintWriter writer, Class<?> baseClass) {
        Map<String, TypeWrapper> typeVariables = new HashMap<>();
        
        writer.print("public class " + baseClass.getSimpleName() + "Impl");
        
        TypeVariableWrapper[] typeParameters = new TypeVariableWrapper[baseClass.getTypeParameters().length];
        for (int i = 0; i < baseClass.getTypeParameters().length; i++) {
            typeParameters[i] = new TypeVariableWrapper(baseClass.getTypeParameters()[i]);
        }
        writer.print(getTypeParametersString(typeParameters, true));
        
        writer.print(" ");
        
        if (baseClass.isInterface()) {
            writer.print("implements");
        } else {
            writer.print("extends");
        }
        
        writer.print(" ");
        
        writer.println(baseClass.getCanonicalName() + getTypeParametersString(typeParameters, false) + " {");
        
        for (int i = 0; i < baseClass.getConstructors().length; i++) {
            ConstructorWrapper constructor = new ConstructorWrapper(baseClass.getConstructors()[i]);
            constructor.setName(baseClass.getSimpleName() + "Impl");
            generateConstructor(writer, constructor);
        }
        
        Map<MethodSignature, MethodWrapper> abstractMethods = new HashMap<>();
        Map<MethodSignature, MethodWrapper> implementedMethods = new HashMap<>();
        
        addAllMethods(TypeWrapper.getWrappedType(baseClass), baseClass, abstractMethods, implementedMethods, typeVariables);
        
        for (Map.Entry<MethodSignature, MethodWrapper> entry : abstractMethods.entrySet()) {
            if (!implementedMethods.containsKey(entry.getKey())) {
                generateMethod(writer, entry.getValue());
            }
        }
        
        writer.println("}");
    }
    
    public static void addAllMethods(TypeWrapper type, Class<?> baseClass, Map<MethodSignature, MethodWrapper> abstractMethods, Map<MethodSignature, MethodWrapper> implementedMethods, Map<String, TypeWrapper> typeVariables) {
        if (type instanceof ClassWrapper) {
            Class<?> c = ((ClassWrapper)type).getWrappedClass();
            
            boolean erase = false;
            
            for (TypeVariable typeVariable : c.getTypeParameters()) {
                if (!typeVariables.containsKey(typeVariable.getName()) && !c.equals(baseClass)) {
                    erase = true;
                }
            }
            
            for (Method method : c.getDeclaredMethods()) {
                MethodWrapper methodWrapper = new MethodWrapper(method);
                
                methodWrapper = methodWrapper.updateTypeVariables(typeVariables);
                
                MethodSignature methodSignature = methodWrapper.getSignature();
                
                if (erase) {
                    methodWrapper = methodWrapper.getErased();
                }
                
                if (Modifier.isAbstract(methodWrapper.getModifiers())) {
                    
                    if (!implementedMethods.containsKey(methodSignature)) {
                        if (abstractMethods.containsKey(methodSignature)) {
                            MethodWrapper other = abstractMethods.get(methodSignature);
                            Class<?> otherReturnType = other.getReturnType().getErased().getWrappedClass();
                            Class<?> returnType = methodWrapper.getReturnType().getErased().getWrappedClass();
                            if (!otherReturnType.equals(returnType)) {
                                if (otherReturnType.isAssignableFrom(returnType)) {
                                    abstractMethods.put(methodSignature, methodWrapper);
                                }
                            }
                        } else {
                            abstractMethods.put(methodSignature, methodWrapper);
                        }
                    }
                } else {
                    if (!implementedMethods.containsKey(methodSignature)) {
                        implementedMethods.put(methodSignature, methodWrapper);
                    }
                }
            }
            
            if (c.getGenericSuperclass() != null) {
                TypeWrapper superclass = TypeWrapper.getWrappedType(c.getGenericSuperclass());
                addAllMethods(superclass, baseClass, abstractMethods, implementedMethods, typeVariables);
            }
            
            for (Type t : c.getGenericInterfaces()) {
                TypeWrapper i = TypeWrapper.getWrappedType(t);
                addAllMethods(i, baseClass, abstractMethods, implementedMethods, typeVariables);
            }
            
            return;
        }
        if (type instanceof ParameterizedTypeWrapper) {
            ParameterizedTypeWrapper parameterizedTypeWrapper = (ParameterizedTypeWrapper)type;
            Map<String, TypeWrapper> updatedTypeVariables = new HashMap<>(typeVariables);
            Class<?> rawType = ((ClassWrapper)parameterizedTypeWrapper.getRawType()).getWrappedClass();
            TypeVariable[] typeParameters = rawType.getTypeParameters();
            for (int i = 0; i < typeParameters.length; i++) {
                TypeWrapper typeParameter = parameterizedTypeWrapper.getActualTypeArguments()[i];
                if (typeParameter instanceof TypeVariableWrapper) {
                    TypeVariableWrapper typeVariable = (TypeVariableWrapper)typeParameter;
                    if (typeVariables.containsKey(typeVariable.getName())) {
                        typeParameter = typeVariables.get(typeVariable.getName());
                    }
                }
                if (!(typeParameter instanceof TypeVariableWrapper) || !((TypeVariableWrapper)typeParameter).getName().equals(typeParameters[i].getName())) {
                    updatedTypeVariables.put(typeParameters[i].getName(), typeParameter);
                }
            }
            addAllMethods(parameterizedTypeWrapper.getRawType(), baseClass, abstractMethods, implementedMethods, updatedTypeVariables);
            return;
        }
        throw new AssertionError("Unreachable code");
    }
    
    public static void generateConstructor(PrintWriter writer, ConstructorWrapper constructor) {
        writer.print("\t" + Modifier.toString(constructor.getModifiers()) + " ");
        
        writer.print(constructor.getName() + "(");
        
        for (int i = 0; i < constructor.getParameterTypes().length; i++) {
            writer.print(constructor.getParameterTypes()[i].toString() + " arg" + i);
            if (i != constructor.getParameterTypes().length - 1) {
                writer.print(", ");
            }
        }
        
        writer.print(") ");
        
        if (constructor.getExceptionTypes().length > 0) {
            writer.print("throws ");
            for (int i = 0; i < constructor.getExceptionTypes().length; i++) {
                writer.write(constructor.getExceptionTypes()[i].toString());
                if (i != constructor.getExceptionTypes().length - 1) {
                    writer.write(", ");
                }
            }
            writer.print(" ");
        }
                    
        writer.println("{");
        writer.print("\t\tsuper(");

        for (int i = 0; i < constructor.getParameterTypes().length; i++) {
            writer.write("arg" + i);
            if (i != constructor.getParameterTypes().length - 1) {
                writer.write(", ");
            }
        }

        writer.println(");");
        writer.println("\t}");
        writer.println();
    }
    
    public static void generateMethod(PrintWriter writer, MethodWrapper method) {
        writer.println("\t@Override");
        writer.print("\t" + Modifier.toString(method.getModifiers() & ~(Modifier.ABSTRACT | Modifier.TRANSIENT)) + " ");

        String typeParameters = getTypeParametersString(method.getTypeParameters(), true);
        if (typeParameters.length() > 0) {
           typeParameters += " ";
        }
        writer.print(typeParameters);

        TypeWrapper returnType = method.getReturnType();
        writer.print(returnType.toString());

        writer.print(" ");
        writer.print(method.getName() + "(");
        TypeWrapper[] parameters = method.getParameterTypes();
        for (int i = 0; i < parameters.length; i++) {
            writer.print(parameters[i].toString() + " arg" + i);
            if (i != parameters.length - 1) {
                writer.print(", ");
            }
        }
        writer.print(") ");
                    
        if (method.getExceptionTypes().length > 0) {
            writer.print("throws ");
            for (int i = 0; i < method.getExceptionTypes().length; i++) {
                writer.print(method.getExceptionTypes()[i].toString());
                if (i != method.getExceptionTypes().length - 1) {
                    writer.print(", ");
                }
            }
            writer.print(" ");
        }
                    
        writer.println("{");
        writer.print("\t\treturn");
        
        if (!method.getReturnType().getErased().getWrappedClass().equals(Void.TYPE)) {
            if (method.getReturnType().getErased().getWrappedClass().equals(Boolean.TYPE)) {
                writer.print(" false");
            } else if (method.getReturnType().getErased().getWrappedClass().isPrimitive()) {
                writer.print(" 0");
            } else {
                writer.print(" null");
            }
        }

        writer.println(";");
        writer.println("\t}");
        writer.println();
    }
    
    public static String getTypeParametersString(TypeVariableWrapper[] parameters, boolean bounds) {
        String result = "";
        if (parameters.length > 0) {
            result += "<";
            for (int i = 0; i < parameters.length; i++) {
                TypeVariableWrapper typeParameter = parameters[i];//.updateTypeVariables(typeVariables);
                result += typeParameter.toString(bounds);
                if (i != parameters.length - 1) {
                    result += ", ";
                }
            }
            result += ">";
        }
        return result;
    }
}
