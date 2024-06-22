/*
 * AbstractObject class. MIT (c) 2024 miktim@mail.ru
 */

package org.miktim.json;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import static org.miktim.json.JsonObject.IGNORED;

abstract class AbstractObject {
    protected Object replacer(String fldName, Object value) {
        return value;
    }

    protected Object reviver(String fldName, Object value) {
        return value;
    }

    @SuppressWarnings("unchecked")
    protected <T> T castMember(String memberName, Json jsonObj, T sample) {
        if (jsonObj.exists(memberName)) {
            return JsonAdapter.cast(jsonObj.get(memberName), sample);
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

    private final transient HashSet<String> ignoredFields = new HashSet<>();//

    protected final boolean isIgnored(String fldName) {
        return ignoredFields.contains(fldName);
    }

    protected final void setIgnored(String[] fldNames) {
        ignoredFields.clear();
        addIgnored(fldNames);
    }

    protected void addIgnored(String[] fldNames) {
        Collections.addAll(ignoredFields, fldNames);
    }

    protected final String[] getIgnored() {
        return ignoredFields.toArray(new String[0]);
    }

    Json unload(AbstractObject thisObj, Object targetObj) // returns Object
            throws IllegalArgumentException, IllegalAccessException {
        String name = targetObj.getClass().getName();
        Object json = thisObj.replacer(name, targetObj);
        if (json.equals(targetObj)) {
            json = new Json();
        }
        if (json instanceof Json) {
            Field[] fields = getAccessibleFields(targetObj);
            for (Field field : fields) {
                name = field.getName();
                if (!thisObj.isIgnored(name)) {
                    field.setAccessible(true);
                    Object value = thisObj.replacer(name, field.get(targetObj));
                    if (value == null || !value.equals(IGNORED)) {
                        if (value != null && value instanceof JsonObject) {
                            value = ((JsonObject) value).unload((JsonObject) value, value);
                        }
                        ((Json) json).set(name, value);
                    }
                }
            }
        }
        return (Json)json;//.normalize(); //???
    }

    <T> T load(AbstractObject thisObj, T targetObj, Object json)
            throws IllegalArgumentException, IllegalAccessException {
        String name = targetObj.getClass().getName();
        json = thisObj.reviver(name, json);
        if (json instanceof Json) {
            Field[] fields = getAccessibleFields(targetObj);
            for (Field field : fields) {
                name = field.getName();
                if (!thisObj.isIgnored(name) && ((Json) json).exists(name)) {
                    field.setAccessible(true);
                    Object newValue = thisObj.reviver(name, ((Json) json).get(name));
                    Object value = field.get(targetObj);
                    if (newValue == null || !newValue.equals(IGNORED)) {
                        if (value != null && value instanceof JsonObject) {
                            field.set(targetObj,
                                    ((JsonObject) value).load((JsonObject) value, value, newValue));
                        } else {
                            field.set(targetObj, JsonAdapter.cast(newValue, value));
                        }
                    }
                }
            }
        }
        return targetObj;
    }

    private Field[] getAccessibleFields(Object obj) {
        LinkedHashMap<String, Field> accessibleFields = new LinkedHashMap<>();
        Class cls = obj.getClass();
        Package pkg = cls.getPackage();
        int ignore = Modifier.FINAL | Modifier.TRANSIENT | Modifier.STRICT
                | Modifier.INTERFACE | Modifier.ABSTRACT | Modifier.NATIVE;
        while (cls != null) {
            Field[] fields = cls.getDeclaredFields();
            Package clsPkg = cls.getPackage();
            for (Field field : fields) {
                if (field.isSynthetic() || field.isEnumConstant()) {
                    continue;
                }
// for different package public or protected fields 
                if (clsPkg != pkg && (field.getModifiers()
                        & (Modifier.PUBLIC | Modifier.PROTECTED)) == 0) {
                    continue;
                }
                String name = field.getName();
// skip overloaded fields                
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
