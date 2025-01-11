/**
 * JsonConverter abstract class, MIT (c) 2024-2025 miktim@mail.ru
 *
 * Converts visible fields of the Java object instance
 * to package native and vice versa.
 * Java final and transient fields are ignored.
 */
package org.miktim.json;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;

public abstract class JsonConverter implements JsonConvertible {

    @Override
    public Object replacer(String name, Object value) {
        return value;
    }

    @Override
    public Object reviver(String name, Object value) {
        return value;
    }

    protected Json unload(JsonConvertible convObj, Object targetObj) {

        try {
            if (targetObj instanceof JsonConvertible) {
                convObj = (JsonConvertible) targetObj;
            }
            Object json = new Json();
            Object fieldsObj = targetObj;
            Object newValue = convObj.replacer(targetObj.getClass().getName(), targetObj);
            if (newValue instanceof Json) {
                json = newValue; // replacer returns Json object
            } else {
                fieldsObj = newValue; // IGNORED?
            }
            Field[] fields = getAccessibleFields(convObj, fieldsObj);
            int ignored = Modifier.FINAL | Modifier.TRANSIENT;
            for (Field field : fields) {
                if ((field.getModifiers() & ignored) != 0) {
                    continue;
                }
                field.setAccessible(true);
                newValue = convObj.replacer(fieldName(field),
                        field.get(targetObj));
                if (newValue == IGNORE) {
                    continue;
                }
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
    protected <T> T load(JsonConvertible convObj, T targetObj, Object json) //{
    {
        try {
            if (json == null || targetObj == null) 
                throw new NullPointerException();
            if (targetObj instanceof JsonConvertible) {
                convObj = (JsonConvertible) targetObj;
            }
            json = convObj.reviver(targetObj.getClass().getName(), json);
            if (json instanceof Json) {
                Field[] fields = getAccessibleFields(convObj, targetObj);
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
                        Object newValue = convObj.reviver(fieldName(field),
                                ((Json) json).get(fieldName));
                        if (newValue == IGNORE) {
                            continue;
                        }
                        Class fieldCls = field.getType();
                        if (!(JSON.isNativeClass(fieldCls) 
                                || newValue == null || value == null
                                || newValue.getClass().equals(fieldCls)
                                || fieldCls.isArray())) {
                            newValue = Json.converter.fromJson(value, (Json) newValue);
                        }
                        field.set(targetObj, JSON.cast(fieldCls, newValue));
                    }
                }
            }
            return (T) targetObj;
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            throw new ClassCastException(ex.getMessage());
        }
    }

    @SuppressWarnings({"unchecked"})
    private Field[] getAccessibleFields(Object convObj, Object targetObj) {
        LinkedHashMap<String, Field> accessibleFields = new LinkedHashMap<>();
        Class convCls = convObj.getClass();
        Package convPkg = convCls.getPackage();
        int ignore = Modifier.STRICT | Modifier.INTERFACE
                | Modifier.ABSTRACT | Modifier.NATIVE;
        Class cls = targetObj.getClass();
        while (cls != null) {
            Field[] fields = cls.getDeclaredFields();
            Package clsPkg = cls.getPackage();
            for (Field field : fields) {
                int modifiers = field.getModifiers();
                if (field.isSynthetic() || field.isEnumConstant()
                        || (modifiers & ignore) != 0) {
                    continue;
                }
                if ((modifiers & Modifier.PUBLIC) == 0) {
// not public field 
// private fields should be excluded for different classes               
                    if (cls != convCls && (modifiers & Modifier.PRIVATE) != 0) {
                        continue;
                    }
                    if (convPkg != clsPkg) {
// inherited protected fields are visible in different packages.
                        if (!(cls.isAssignableFrom(convCls) 
                                && (modifiers & Modifier.PROTECTED) > 0)) {
                            continue;
                        }
                    }
                }
                String name = field.getName();
// skip overridden fields                
                if (!accessibleFields.containsKey(name)) {
                    accessibleFields.put(name, field);
                }
            }
            cls = cls.getSuperclass();
        }
        return accessibleFields.values().toArray(new Field[0]);
    }

    private String fieldName(Field field) {
        return field.getDeclaringClass().getName() + ":" + field.getName();
    }
}
