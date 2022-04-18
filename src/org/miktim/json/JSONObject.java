/*
 * JSONObject abstract class. MIT (c) 2022 miktim@mail.ru
 *
 * Unloads/loads Java object accessible fields to/from JSON object.
 * - Java final, interface, abstract, transient, strict fields are ignored;
 * - see JSON set/get/cast rules for Java object fields in the notes for JSON object
 *   and JSONAdapter.
 *
 * Created: april 2022
 */
package org.miktim.json;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.LinkedHashMap;

public abstract class JSONObject {

    public static final Object IGNORED = new Object();

    public Object toJSON()
            throws IllegalArgumentException, IllegalAccessException {
        return toJSON(this);
    }

    protected Object replacer(String fldName, Object value) {
        return value;
    }

    public void fromJSON(Object json)
            throws IllegalArgumentException, IllegalAccessException {
        fromJSON(this, json);
    }

    @SuppressWarnings("unchecked")
    protected <T> T castMember(String memberName, JSON jsonObj, T sample) {
        if (jsonObj.exists(memberName)) {
            return JSONAdapter.cast(jsonObj.get(memberName), sample);
        }
        return sample;
    }

    protected Object reviver(String fldName, Object value) {
        return value;
    }

    public static boolean isClassName(String fldName) {
        try {
            Class.forName(fldName);
        } catch (ClassNotFoundException ex) {
            return false;
        }
        return true;
    }

    private String[] ignoredFields = new String[]{};//

    protected final boolean isIgnored(String fldName) {
        return Arrays.binarySearch(ignoredFields, fldName) >= 0;
    }

    protected void setIgnored(String[] fldNames) {
        ignoredFields = fldNames.clone();
        Arrays.sort(ignoredFields);
    }

    protected String[] getIgnored() {
        return ignoredFields;
    }

    private Object toJSON(JSONObject jsonObj) // returns Object
            throws IllegalArgumentException, IllegalAccessException {
        String name = jsonObj.getClass().getName();
        Object json = jsonObj.replacer(name, new JSON());
        if (json instanceof JSON) {
//            Field[] fields = jsonObj.getClass().getDeclaredFields();
            Field[] fields = getAccessibleFields(jsonObj);
            for (Field field : fields) {
                name = field.getName();
//                if (!jsonObj.isIgnored(field)) {
                if (!jsonObj.isIgnored(name)) {
                    field.setAccessible(true);
                    Object value = jsonObj.replacer(name, field.get(jsonObj));
                    if (value == null || !value.equals(IGNORED)) {
                        if (value != null && value instanceof JSONObject) {
                            value = ((JSONObject) value).toJSON(((JSONObject) value));
                        }
                        ((JSON) json).set(name, value);
                    }
                }
            }
        }
        return json;//.normalize(); //???
    }

    private JSONObject fromJSON(JSONObject jsonObj, Object json)
            throws IllegalArgumentException, IllegalAccessException {
        String name = jsonObj.getClass().getName();
        json = jsonObj.reviver(name, json);
        if (json instanceof JSON) {
//            Field[] fields = jsonObj.getClass().getDeclaredFields();
            Field[] fields = getAccessibleFields(jsonObj);
            for (Field field : fields) {
                name = field.getName();
//                if (!jsonObj.isIgnored(field) && ((JSON) json).exists(name)) {
                if (!jsonObj.isIgnored(name) && ((JSON) json).exists(name)) {
                    field.setAccessible(true);
                    Object newValue = jsonObj.reviver(name, ((JSON) json).get(name));
                    Object value = field.get(jsonObj);
                    if (newValue == null || !newValue.equals(IGNORED)) {
                        if (value != null && value instanceof JSONObject) {
                            field.set(jsonObj,
                                    ((JSONObject) value).fromJSON((JSONObject) value, newValue));
                        } else {
                            field.set(jsonObj, JSONAdapter.cast(newValue, value));
                        }
                    }
                }
            }
        }
        return jsonObj;
    }

    /*
    private boolean isIgnored(Field field) {
        return field.isSynthetic() || field.isEnumConstant() //|| field.isAccessible() deprecated
                || isIgnored(field.getName())
                || (field.getModifiers() & Modifier.FINAL) != 0;
    }*/

    private Field[] getAccessibleFields(Object obj) {
        LinkedHashMap<String, Field> accessibleFields = new LinkedHashMap<>();
        Class cls = obj.getClass();
        Package pkg = cls.getPackage();
        int ignore = Modifier.FINAL | Modifier.TRANSIENT | Modifier.STRICT
                | Modifier.INTERFACE | Modifier.ABSTRACT;
        while (cls != null) {
            Field[] fields = cls.getDeclaredFields();
// for different package ignore NATIVE
            int ignored = ignore | (pkg != cls.getPackage() ? Modifier.NATIVE : 0);
            for (Field field : fields) {
                String name = field.getName();
                if (!accessibleFields.containsKey(name)
                        && (field.getModifiers() & ignored) == 0) {
                    accessibleFields.put(name, field);
                }
            }
// for all super classes disable PRIVATE
            ignore |= Modifier.PRIVATE;
            cls = cls.getSuperclass();
        }
        return accessibleFields.values().toArray(new Field[]{});
    }

}
