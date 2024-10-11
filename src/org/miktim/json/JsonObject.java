/*
 * JsonObject abstract class. MIT (c) 2022-2024 miktim@mail.ru
 + Java object extender
 *
 * Unloads/loads Java object accessible fields to/from JSON object.
 * - Java final, transient fields are ignored;
 *   TODO initialized final object fields?
 * - see the notes on the Json methods set/get/cast.
 *
 * Created: april 2022
 */
package org.miktim.json;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;

public abstract class JsonObject {// { 

    public final Json toJson() {
//            throws IllegalArgumentException, IllegalAccessException {
        return unload(this, this);
    }

    @SuppressWarnings("unchecked")
    public final <T> T fromJson(Json json) {
//            throws IllegalArgumentException, IllegalAccessException {
        return (T) load(this, this, json);
    }

    public final <T> T fromJson(T target, Json json) {
//            throws IllegalArgumentException, IllegalAccessException {
        return (T) load(this, target, json);
    }

    public final Json toJson(Object target) {
//            throws IllegalArgumentException, IllegalAccessException {
        return unload(this, target);
    }

    @Override
    public String toString() {
        return toJson().toString();
    }

    protected static final transient Object IGNORED = new Object();

    private transient Object target;

    @SuppressWarnings("unchecked")
    protected <T> T getTarget() {
//        Class<T> cls = (Class<T>) target.getClass();
        return (T) target;
    }

    protected Object replacer(String name, Object value) {
        return value;
    }

    protected Object reviver(String name, Object value) {
        return value;
    }

    protected void onError(String name, Exception e) {
        System.err.println(name + " " + e.getMessage());
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
// https://stackoverflow.com/questions/21226648/how-to-test-if-an-object-is-primitive-type-or-an-array-of-primitive-type-in-java
    boolean isJsonType(Object obj) {
        Class<?> c = obj.getClass();
        return c.isPrimitive()
                || c.equals(String.class)
                || c.equals(String[].class)
                || c.equals(Json.class)
                || c.equals(Json[].class)
                || c.isArray() && c.getComponentType().isPrimitive();
    }
     */
    Json unload(JsonObject thisObj, Object targetObj) {// returns Object
//            throws IllegalArgumentException, IllegalAccessException {
        if (targetObj instanceof JsonObject) {
            thisObj = (JsonObject) targetObj;
        }
// TODO: context stack push/pop
        Object oldTarget = this.target; // save current target object
        this.target = targetObj;
// first call replacer with class name and target object        
        Object json = thisObj.replacer(targetObj.getClass().getName(), targetObj);
        if (!(json instanceof Json)) {
            json = new Json(); // replacer returns target object
        }
        Field[] fields = getAccessibleFields(thisObj, targetObj);
        for (Field field : fields) {
            String fieldName = field.getName();
//                if (!thisObj.isIgnored(fieldName)) continue;
            field.setAccessible(true);
            try {
                Object value = thisObj.replacer(fieldName(field),
                        field.get(targetObj));
                if (value == null || !value.equals(IGNORED)) {
                    if (value instanceof JsonObject) {
                        value = unload(thisObj, value);
                    }
                    ((Json) json).set(fieldName, value);
                }
            } catch (Exception ex) {
                thisObj.onError(fieldName(field), ex);
//                System.err.println(e.getMessage());
            }
        }

        this.target = oldTarget; // restore target object
        return (Json) json;//.normalize(); //???
    }

    /*
    @SuppressWarnings("unchecked")
    <T> T loadArray(JsonObject thisObj, T value, Object newValue) {
        int length = Array.getLength(newValue);
        Class<?> cls = value.getClass().getComponentType();
        T array = (T) Array.newInstance(cls, length);
        for (int i = 0; i < length; i++) {
            Array.set(array, i, load(thisObj, Array.get(value, i), Array.get(newValue, i)));
        }
        return (T) array;
    }
     */
    <T> T load(JsonObject thisObj, T targetObj, Object json) {
//            throws IllegalArgumentException, IllegalAccessException {
        if (targetObj instanceof JsonObject) {
            thisObj = (JsonObject) targetObj;
        }
        Object oldTarget = this.target;
        this.target = targetObj;
        json = thisObj.reviver(targetObj.getClass().getName(), json);
        if (json instanceof Json) {
            Field[] fields = getAccessibleFields(thisObj, targetObj);
            for (Field field : fields) {
                String fieldName = field.getName();
//                if (thisObj.isIgnored(fieldName) continue;
                if (((Json) json).exists(fieldName)) {
                    field.setAccessible(true);
                    try {
                        Object newValue = thisObj.reviver(fieldName(field),
                                ((Json) json).get(fieldName));
                        Object value = field.get(targetObj);
                        if ((field.getModifiers() & Modifier.FINAL) == 0
                                && (newValue == null || !newValue.equals(IGNORED))) {
                            if (value != null && value instanceof JsonObject) {
                                field.set(targetObj, load(thisObj, value, newValue));
                            } else {
                                field.set(targetObj, JSON.cast(newValue, value));
                            }
                        }
                    } catch (Exception ex) {
                        thisObj.onError(fieldName(field), ex);
//                        System.err.println(ex.getMessage());
                    }
                }
            }
        }
        this.target = oldTarget;
        return (T) targetObj;
    }

    private String fieldName(Field field) {
        return field.getDeclaringClass().getName() + "." + field.getName();
    }

    private Field[] getAccessibleFields(Object thisObj, Object targetObj) {
        LinkedHashMap<String, Field> accessibleFields = new LinkedHashMap<>();
        Class thisCls = thisObj.getClass();
        Package thisPkg = thisCls.getPackage();
        int ignore = Modifier.PRIVATE | Modifier.TRANSIENT | Modifier.STRICT
                | Modifier.INTERFACE | Modifier.ABSTRACT | Modifier.NATIVE;
        Class cls = targetObj.getClass();
        while (cls != null) {
            Field[] fields = cls.getDeclaredFields();
            Package clsPkg = cls.getPackage();
            for (Field field : fields) {
                if (field.isSynthetic() || field.isEnumConstant()) {
                    continue;
                }
// for different package public fields 
                if (clsPkg != thisPkg && (field.getModifiers()
                        & Modifier.PUBLIC) == 0) {
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
//            ignore |= Modifier.PRIVATE;
            cls = cls.getSuperclass();
        }
        return accessibleFields.values().toArray(new Field[]{});
    }

    /*    
    protected static final class IgnoredFields extends HashSet<String> {
        IgnoredFields() {
            super();
        }
    }
    protected transient IgnoredFields ignoredFields = new IgnoredFields();

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
     */
}
