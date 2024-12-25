/**
 * JsonConverter abstract class, MIT (c) 2024 miktim@mail.ru
 *
 * Converts visible fields of the Java object instance
 * to package native and vice versa. 
 * Java final and transient fields are ignored.
 */
package org.miktim.json;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;

abstract class JsonConverter implements JsonConvertible {
    
    protected static final transient Object IGNORED = new Object();

    @Override
    public Object replacer(String name, Object value) {
        return value;
    }

    @Override
    public Object reviver(String name, Object value) {
        return value;
    }
    
    protected Json unload(JsonConvertible thisObj, Object targetObj) {

        try {
            if (targetObj instanceof JsonConvertible) {
                thisObj = (JsonConvertible) targetObj;
            }
            Object json = thisObj.replacer(targetObj.getClass().getName(), targetObj);
            if (!(json instanceof Json)) {
                json = new Json(); // replacer returns target object
            }
            Field[] fields = getAccessibleFields(thisObj, targetObj);
            int ignored = Modifier.FINAL | Modifier.TRANSIENT;
            for (Field field : fields) {
                if ((field.getModifiers() & ignored) != 0) {
                    continue;
                }
                field.setAccessible(true);
                Object newValue = thisObj.replacer(fieldName(field),
                        field.get(targetObj));
                if (newValue.equals(IGNORED)) {
                    continue;
                }
// TODO: !? newValue == null
//                Class fieldCls = fieldType();
//                if (!(JSON.isNativeClass(fieldCls) || newValue.getClass().isArray())) {
                if (!(JSON.isNativeType(newValue) || newValue.getClass().isArray())) {
                    newValue = Json.converter.toJson(newValue);
                }
                ((Json) json).set(field.getName(), newValue);
            }
            return (Json) json;
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            throw new ClassCastException(ex.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    protected <T> T load(JsonConvertible thisObj, T targetObj, Object json) //{
    {
        try {
            if (targetObj instanceof JsonConvertible) {
                thisObj = (JsonConvertible) targetObj;
            }
            json = thisObj.reviver(targetObj.getClass().getName(), json);
            if (json instanceof Json) {
                Field[] fields = getAccessibleFields(thisObj, targetObj);
                int ignored = Modifier.FINAL | Modifier.TRANSIENT;
                for (Field field : fields) {
                    if ((field.getModifiers() & ignored) != 0) {
                        continue;
                    }
                    String fieldName = field.getName();
                    if (((Json) json).exists(fieldName)) {
                        field.setAccessible(true);
                        Object value = field.get(targetObj); // sample for casting
// 
                        Object newValue = thisObj.reviver(fieldName(field),
                                ((Json) json).get(fieldName));
                        if (newValue.equals(IGNORED)) {
                            continue;
                        }
// TODO: !? field value == null
                        Class fieldCls = field.getType();
                        if (!(JSON.isNativeClass(fieldCls) || value.getClass().isArray())) {
//                        if (!(JSON.isNativeType(value) || value.getClass().isArray())) {
                            newValue = Json.converter.fromJson(value, newValue);
                        }
                        field.set(targetObj, JSON.cast(fieldCls, newValue));
//                        field.set(targetObj, JSON.cast(value, newValue));
                    }
                }
            }
            return (T) targetObj;
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            throw new ClassCastException(ex.getMessage());
        }
    }

    private Field[] getAccessibleFields(Object thisObj, Object targetObj) {
        LinkedHashMap<String, Field> accessibleFields = new LinkedHashMap<>();
        Class thisCls = thisObj.getClass();
        Package thisPkg = thisCls.getPackage();
        int ignore = Modifier.STRICT | Modifier.INTERFACE
                | Modifier.ABSTRACT | Modifier.NATIVE;
        Class cls = targetObj.getClass();
        while (cls != null) {
            Field[] fields = cls.getDeclaredFields();
            Package clsPkg = cls.getPackage();
            for (Field field : fields) {
//                if (isIgnored(fieldName) continue;
                int modifiers = field.getModifiers();
                if (field.isSynthetic() || field.isEnumConstant()
                        || (modifiers & ignore) != 0) {
                    continue;
                }
// only public and protected fields are available for different packages 
                if (clsPkg != thisPkg
                        && (modifiers & (Modifier.PUBLIC | Modifier.PROTECTED)) == 0) {
                    continue;
                }
// private fields should be excluded for different classes               
                if (cls != thisCls && (modifiers & Modifier.PRIVATE) != 0) {
                    continue;
                }
                String name = field.getName();
// skip overridden fields                
                if (!accessibleFields.containsKey(name)) {
                    accessibleFields.put(name, field);
                }
            }
            cls = cls.getSuperclass();
        }
        return accessibleFields.values().toArray(new Field[]{});
    }

    private String fieldName(Field field) {
        return field.getDeclaringClass().getName() + ":" + field.getName();
    }

/*
    public static boolean isClassName(String fldName) {
        try {
            Class.forName(fldName);
        } catch (ClassNotFoundException ex) {
            return false;
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    public static <T> T castMember(T sample, String memberName, Json jsonObj) {
        if (jsonObj.get(memberName) != null) {
            return JSON.cast(sample, jsonObj.get(memberName));
        }
        return sample;
    }
*/
    /*
    private transient Object target;
    @SuppressWarnings("unchecked")
    protected <T> T getTarget() {
        return (T) target;
    }

    private transient HashSet<String> ignoredFields = new HashSet<>();

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
     */

}
