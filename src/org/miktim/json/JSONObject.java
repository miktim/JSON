/*
 * JSONObject class. MIT (c) 2022 miktim@mail.ru
 */
package org.miktim.json;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;

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
            return JSONAdapter.castTo(jsonObj.get(memberName), sample);
        }
        return sample;
    }

    protected Object reviever(String fldName, Object value) {
        return value;
    }

    protected static boolean isClassName(String fldName) {
        try {
            Class.forName(fldName);
        } catch (ClassNotFoundException ex) {
            return false;
        }
        return true;
    }

    private String[] _ignoredFields = new String[]{};//

    protected boolean isIgnored(String fldName) {
        return Arrays.binarySearch(_ignoredFields, fldName) >= 0;
    }

    protected void setIgnored(String[] fldNames) {
        _ignoredFields = fldNames.clone();
        Arrays.sort(_ignoredFields);
    }

    protected String[] getIgnored() {
        return _ignoredFields;
    }

    private Object toJSON(JSONObject jsonObj) // returns Object
            throws IllegalArgumentException, IllegalAccessException {
        String name = jsonObj.getClass().getName();
        Object json = jsonObj.replacer(name, new JSON());
        if (json instanceof JSON) {
            Field[] fields = jsonObj.getClass().getDeclaredFields();
            for (Field field : fields) {
                if (!jsonObj.isIgnored(field)) {
                    name = field.getName();
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
        json = jsonObj.reviever(name, json);
        if (json instanceof JSON) {
            Field[] fields = jsonObj.getClass().getDeclaredFields();
            for (Field field : fields) {
                name = field.getName();
                if (!jsonObj.isIgnored(field) && ((JSON) json).exists(name)) {
                    field.setAccessible(true);
                    Object newValue = jsonObj.reviever(name, ((JSON) json).get(name));
                    Object value = field.get(jsonObj);
                    if (newValue == null || !newValue.equals(IGNORED)) {
                        if (value != null && value instanceof JSONObject) {
                            field.set(jsonObj,
                                    ((JSONObject) value).fromJSON((JSONObject) value, newValue));
                        } else {
                            field.set(jsonObj, JSONAdapter.castTo(newValue, value));
                        }
                    }
                }
            }
        }
        return jsonObj;
    }

    private boolean isIgnored(Field field) {
        return field.isSynthetic() || field.isEnumConstant() //|| field.isAccessible() deprecated
                || isIgnored(field.getName())
                || (field.getModifiers() & Modifier.FINAL) != 0;
    }

}
