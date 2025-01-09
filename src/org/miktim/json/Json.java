/**
 * Json class, MIT (c) 2020-2024 miktim@mail.ru
 * Java representation of a JSON object.
 *
 * Release notes:
 * - Java 7+, Android compatible;
 * - Json members:
 *   Json object, String, Number, Boolean, null, Object[] array of listed types;
 * - Json setters accept any Java objects, all Java primitives and their arrays;
 * - avoid recursion!.
 *
 * Created: 2020-03-07
 */
package org.miktim.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.text.ParseException;
import java.util.LinkedHashMap;

public class Json extends LinkedHashMap<String, Object> {

    public Json(InputStream inStream) throws IOException, ParseException {
        super();
        this.putAll((Json) JSON.fromJSON(inStream, "UTF-8"));
    }

    public Json(String jsonText) throws IOException, ParseException {
        super();
        this.putAll((Json) JSON.fromJSON(jsonText));//??? another way
    }

// Memebers: name,value pairs    
    public Json(Object... members) throws IndexOutOfBoundsException {
        super();
        for (int i = 0; i < members.length;) {
            this.set(members[i++], members[i++]);
        }
    }

    public String toJSON() throws IOException {
        return JSON.toJSON(this);
    }

    public String toJSON(String memberName) throws IOException {
        return JSON.toJSON(this.get(memberName));
    }

    public String toJSON(String memberName, int... indices) throws IOException {
        return JSON.toJSON(this.get(memberName, indices));
    }

    public Json toJSON(OutputStream outStream) throws IOException {
        return JSON.toJSON(this, outStream, 0, "UTF-8");
    }

    @Override
    public String toString() {
        try {
            return JSON.toJSON(this);
        } catch (IOException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    public String[] listNames() {
        return this.keySet().toArray(new String[0]);
    }

    public boolean exists(String memberName, int... indices) {
        if (!this.containsKey(memberName)) {
            return false;
        }
        try {
            get(memberName, indices);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @SuppressWarnings("unchecked")
// for parser, no checking values
    <T> T superPut(String key, T value) {
        return (T) super.put(key, value);
    }
            
    @Override
    public Object put(String key, Object value) {
        
        if (value != null) {
            Class cls = value.getClass();
            if (!cls.isPrimitive()) {
                try {
// TODO: something faster than a double conversion
                    value = JSON.fromJSON(JSON.toJSON(value));
                } catch (IOException | ParseException ex) {
                }
            }
        }
        
        return super.put(key == null ? "null" : key, value);
    }

    public Json set(Object memberName, Object value) { //Object value) {
        put(String.valueOf(memberName), value);
        return this;
    }

    @SuppressWarnings({"unchecked"})
    public <T>T get(Object memberName, int... indices) {
        T obj = (T)get(String.valueOf(memberName));
        for (int i = 0; i < indices.length; i++) {
          obj = (T)Array.get(obj, indices[i]);
        }
        return (T)obj;
    }

    public Json getJson(String memberName, int... indices) {
        return (Json) get(memberName, indices);
    }

    public Number getNumber(String memberName, int... indices) {
        return (Number) get(memberName, indices);
    }

    public String getString(String memberName, int... indices) {
        return (String) get(memberName, indices);
    }

    public Boolean getBoolean(String memberName, int... indices) {
        return (Boolean) get(memberName, indices);
    }

    public Object[] getArray(String memberName, int... indices) {
        Object obj = get(memberName, indices);
        Class cls = obj.getClass();
        if (cls == Object[].class) {
            return (Object[]) obj;
        } else if (cls.isArray()) {
//            return (Object[]) JSON.cast(Object[].class, obj);
        }
        return (Object[]) obj; // throws ClassCastException
    }


// casting by sample or class the value of a member or element of an array
    @SuppressWarnings("unchecked")
    public <T> T castMember(T sample, String memberName, int... indices) {
        return JSON.cast(sample, (T) get(memberName, indices));
    }

    @SuppressWarnings("unchecked")
    public <T> T castMember(Class<T> cls, String memberName, int... indices) {
        return JSON.cast(cls, (T) get(memberName, indices));
    }
/*
    public Json normalize() throws IOException, ParseException {
        return (Json) JSON.fromJSON(this.toString()); // :)
    }
*/
    public static class Converter extends JsonConverter {

        public Converter() {

        }

    @SuppressWarnings("unchecked")
    public Json toJson(Object targetObj) {
//            throws IllegalArgumentException, IllegalAccessException {
        return unload(this, targetObj);
    }

    @SuppressWarnings("unchecked")
    public <T> T fromJson(Object targetObj, Json jsonObj) {
//            throws IllegalArgumentException, IllegalAccessException {
        return (T) load(this, targetObj, jsonObj);
    }
/*
        @Override
        public Object replacer(String name, Object value) {
             return value;
        }

        @Override
        public Object reviver(String name, Object value) {
            return value;
        }
*/
    }    

    public static Converter converter = new Converter();
}
