/*
 * JsonObject abstract class. MIT (c) 2022-2024 miktim@mail.ru
 + Java object extender
 *
 * Unloads/loads Java object accessible fields to/from Json object.
 * - Java final, transient fields are ignored;
 * - see the notes on the Json methods set/get/cast.
 *
 * Created: april 2022
 */
package org.miktim.json;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;

public abstract class JsonObject {

    @SuppressWarnings("unchecked")
    public Json toJson()
            throws IllegalArgumentException, IllegalAccessException {
        return unload(this, this);
    }

    @SuppressWarnings("unchecked")
    public Json toJson(Object targetObj)
            throws IllegalArgumentException, IllegalAccessException {
        return unload(this, targetObj);
    }

    @SuppressWarnings("unchecked")
    public <T> T fromJson(Object nativeObj)
            throws IllegalArgumentException, IllegalAccessException {
        return (T)load(this, this, nativeObj);
    }

    @SuppressWarnings("unchecked")
    public <T> T fromJson(Object targetObj, Object jsonObj)
            throws IllegalArgumentException, IllegalAccessException {
        return (T) load(this, targetObj, jsonObj);
    }

    protected static final transient Object IGNORED = new Object();

    protected Object replacer(String name, Object value) {
        return value;
    }

    protected Object reviver(String name, Object value) {
        return value;
    }

    @Override
    public String toString() {
        try {
            return JSON.toJSON(JsonObject.this.toJson());
        } catch (IllegalArgumentException | ReflectiveOperationException | IOException ex) {
            ex.printStackTrace();
            return ex.getMessage();
        }
    }

    protected Json unload(JsonObject thisObj, Object targetObj) //{// returns Object
            throws IllegalArgumentException, IllegalAccessException {
        if (targetObj instanceof JsonObject) {
            thisObj = (JsonObject) targetObj;
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
            String fieldName = field.getName();

            field.setAccessible(true);
            Object value = field.get(targetObj);

            Object newValue = thisObj.replacer(fieldName(targetObj, field),
                    field.get(targetObj));
            if (newValue.equals(IGNORED)) {
                continue;
            }
            if (value instanceof JsonObject && !thisObj.equals(value)) {
                newValue = thisObj
                        .unload(thisObj, newValue);
            }
            if (!JSON.isNativeType(newValue) && !JSON.isNativeType(value)) {
                newValue = unload(thisObj, newValue);
            }
            ((Json) json).set(fieldName, newValue);

        }
        return (Json) json;//.normalize(); //???
    }

    protected <T> T load(JsonObject thisObj, T targetObj, Object json) //{
            throws IllegalArgumentException, IllegalAccessException {
        if (targetObj instanceof JsonObject) {
            thisObj = (JsonObject) targetObj;
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
// TODO  fieldname of JsonObject field
                    Object newValue = thisObj.reviver(fieldName(targetObj, field),
                            ((Json) json).get(fieldName));
                    if (newValue.equals(IGNORED)) {
                        continue;
                    }
                    if (value instanceof JsonObject) {
                        newValue = load((JsonObject) value, value, newValue);
                        thisObj = (JsonObject) value;
                    }
//                    if (!JSON.isNativeType(newValue) || (newValue instanceof Json)) {
                    if (!JSON.isNativeType(value)) {
                        newValue = load(thisObj, value, newValue);
                    }
                    field.set(targetObj, JSON.cast(value, newValue));
                }
            }
        }
        return (T) targetObj;
    }

    private Field[] getAccessibleFields(Object thisObj, Object targetObj) {
        LinkedHashMap<String, Field> accessibleFields = new LinkedHashMap<>();
        Class thisCls = thisObj.getClass();
        Package thisPkg = thisCls.getPackage();
        int ignore = Modifier.STRICT
                | Modifier.INTERFACE | Modifier.ABSTRACT | Modifier.NATIVE;
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
// for different packages: public and protected fields 
                if (clsPkg != thisPkg
                        && (modifiers & (Modifier.PUBLIC | Modifier.PROTECTED)) == 0) {
                    continue;
                }
// for different classes except private               
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

    public static boolean isClassName(String fldName) {
        try {
            Class.forName(fldName);
        } catch (ClassNotFoundException ex) {
            return false;
        }
        return true;
    }

    private String fieldName(Object target, Field field) {
        return target.getClass().getName() + "." + field.getName();
    }
// TODO ???

    @SuppressWarnings("unchecked")
    protected <T> T castMember(T sample, String memberName, Json jsonObj) {
        if (jsonObj.get(memberName) != null) {
            return JSON.cast(sample, jsonObj.get(memberName));
        }
        return sample;
    }
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
