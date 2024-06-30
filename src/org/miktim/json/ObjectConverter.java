/*
 * AbstractObject class. MIT (c) 2024 miktim@mail.ru
 */
package org.miktim.json;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;

abstract class ObjectConverter {

    protected static final transient Object IGNORED = new Object();

    protected Object replacer(String fldName, Object value) {
        return value;
    }

    protected Object reviver(String fldName, Object value) {
        return value;
    }

    @SuppressWarnings("unchecked")
    protected <T> T castMember(String memberName, Json jsonObj, T sample) {
        if (jsonObj.get(memberName) != null) {
            return JSON.cast(jsonObj.get(memberName), sample);
        }
        return sample;
    }

    public static boolean isClassName(String fldName) {
        try {
            Class.forName(fldName);
        } catch (ClassNotFoundException ex) {
            return false;
        }
        return true;
    }
/*    
    protected static final class IgnoredFields extends HashSet<String> {
        IgnoredFields() {
            super();
        }
    }
    protected transient IgnoredFields ignoredFields = new IgnoredFields();
*/    
    protected transient HashSet<String> ignoredFields = new HashSet<>();

    protected final boolean isIgnored(String fldName) {
        return ignoredFields.contains(fldName);
    }

    protected final void setIgnored(String[] fldNames) {
        ignoredFields.clear();
        addIgnored(fldNames);
    }

    protected final void addIgnored(String[] fldNames) {
        Collections.addAll(ignoredFields, fldNames);
    }

    protected final String[] getIgnored() {
        return ignoredFields.toArray(new String[0]);
    }

    Json unload(ObjectConverter thisObj, Object targetObj) // returns Object
            throws IllegalArgumentException, IllegalAccessException {
        if (targetObj instanceof JsonObject) {
            thisObj = (ObjectConverter) targetObj;
        }
        String className = targetObj.getClass().getName();
        Object json = thisObj.replacer(className, targetObj);
        if (json.equals(targetObj)) {
            json = new Json();
        }
        if (json instanceof Json) {
            Field[] fields = getAccessibleFields(thisObj,targetObj);
            for (Field field : fields) {
                String fieldName = field.getName();
                if (!thisObj.isIgnored(fieldName)) {
                    field.setAccessible(true);
                    Object value = thisObj.replacer(fieldName, field.get(targetObj));
                    if (value == null || !value.equals(IGNORED)) {
                        if (value != null && value instanceof JsonObject) {
                            value = ((JsonObject) value).unload((JsonObject) value, value);
                        }
                        ((Json) json).set(fieldName, value);
                    }
                }
            }
        }
        return (Json) json;//.normalize(); //???
    }

    <T> T load(ObjectConverter thisObj, T targetObj, Object json)
            throws IllegalArgumentException, IllegalAccessException {
        if (targetObj instanceof JsonObject) {
            thisObj = (ObjectConverter) targetObj;
        }
        String className = targetObj.getClass().getName();
        json = thisObj.reviver(className, json);
        if (json instanceof Json) {
            Field[] fields = getAccessibleFields(thisObj, targetObj);
            for (Field field : fields) {
                String fieldName = field.getName();
                if (!thisObj.isIgnored(fieldName) && ((Json) json).exists(fieldName)) {
                    field.setAccessible(true);
                    Object newValue = thisObj.reviver(fieldName, ((Json) json).get(fieldName));
                    Object value = field.get(targetObj);
                    if ((field.getModifiers() & Modifier.FINAL) == 0 && 
                            (newValue == null || !newValue.equals(IGNORED))) {
                        if (value != null && value instanceof JsonObject) {
                            field.set(targetObj,
                                    ((JsonObject) value).load((JsonObject) value, value, newValue));
                        } else {
                            field.set(targetObj, JSON.cast(newValue, value));
                        }
                    }
                }
            }
        }
        return  (T) targetObj;
    }

    private Field[] getAccessibleFields(Object thisObj, Object targetObj) {
        LinkedHashMap<String, Field> accessibleFields = new LinkedHashMap<>();
        Class thisCls =thisObj.getClass();
        Package thisPkg = thisCls.getPackage();
        int ignore = Modifier.TRANSIENT | Modifier.STRICT
                | Modifier.INTERFACE | Modifier.ABSTRACT | Modifier.NATIVE;
        Class cls = targetObj.getClass();
        while (cls != null) {
            Field[] fields = cls.getDeclaredFields();
            Package clsPkg = cls.getPackage();
            for (Field field : fields) {
                if (field.isSynthetic() || field.isEnumConstant()) {
                    continue;
                }
// for different package public or protected fields 
                if (clsPkg != thisPkg && (field.getModifiers()
                        & (Modifier.PUBLIC | Modifier.PROTECTED)) == 0) {
                    continue;
                }
                String name = field.getName();
// skip overriden fields                
                if (!accessibleFields.containsKey(name)
                        && (field.getModifiers() & ignore) == 0) {
                    accessibleFields.put(name, field);
                }
            }
// for other classes exclude private fields            
            ignore |= Modifier.PRIVATE;
            cls = cls.getSuperclass();
        }
        return accessibleFields.values().toArray(new Field[]{});
    }

}
