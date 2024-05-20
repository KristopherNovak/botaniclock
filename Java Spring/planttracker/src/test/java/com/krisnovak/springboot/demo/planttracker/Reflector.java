package com.krisnovak.springboot.demo.planttracker;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class Reflector {

    public static void setField(Object theObject, String fieldName, Object theValue){
        Field field = getFieldViaReflection(theObject, fieldName);
        field.setAccessible(true);
        try{field.set(theObject, theValue);}
        catch(IllegalAccessException e){
            throw new RuntimeException("Something went wrong");
        }
    }

    public static Object getField(Object theObject, String fieldName){
        Field field = getFieldViaReflection(theObject, fieldName);
        field.setAccessible(true);
        try{return field.get(theObject);}
        catch(IllegalAccessException e){
            throw new RuntimeException("Something went wrong");
        }
    }

    private static Field getFieldViaReflection(Object theObject, String fieldName){
        Field field;
        try{field = theObject.getClass().getDeclaredField(fieldName);}
        catch(NoSuchFieldException e){
            throw new RuntimeException("Field with given name does not exist");
        }
        return field;
    }

    public static Object invokeMethod(Object theObject, String methodName, Object[] argObjects){

        Class[] argClasses = new Class[argObjects.length];

        for(int i=0; i < argObjects.length; i++){
            argClasses[i] = argObjects[i].getClass();
        }

        Method method = getMethodViaReflection(theObject, methodName, argClasses);
        method.setAccessible(true);
        try{return method.invoke(theObject, argObjects);}
        catch(IllegalAccessException e){
            throw new RuntimeException("Method could not be set to accessible");
        }
        catch(InvocationTargetException e){
            throw new RuntimeException("The invoked method called an exception");
        }
    }

    private static Method getMethodViaReflection(Object theObject, String methodName, Class[] argClasses){
        Method method;
        try{method = theObject.getClass().getDeclaredMethod(methodName, argClasses);}
        catch(NoSuchMethodException e){
            throw new RuntimeException("Method does not exist for the provided object");
        }
        return method;

    }
}
