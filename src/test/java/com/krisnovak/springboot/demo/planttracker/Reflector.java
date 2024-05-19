package com.krisnovak.springboot.demo.planttracker;

import java.lang.reflect.Field;

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
            throw new RuntimeException("The field name has been changed");
        }
        return field;
    }
}
