/**
 * Json class, MIT (c) 2020-2024 miktim@mail.ru
 * Java representation of a JSON object.
 * 
 * Release notes:
 * - Java 7+, Android compatible;
 * - Json members:
 *   Json object, String, Number (Double or Long), Boolean, null, Object[] array of listed types;
 * - Json object members (name/value pairs) are stored in creation/appearance order;
 * - Json object setter accepts any Java object, all Java primitives and primitive arrays;
 * - avoid recursion!;
 *
 * Created: 2020-03-07
 */
package org.miktim.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.lang.reflect.Array;
import java.text.ParseException;

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
            this.put(String.valueOf(members[i++]), members[i++]);
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
            ex.printStackTrace();
            return null;
        }
    }

    public String[] listNames() {
        return this.keySet().toArray(new String[0]);
    }

    public boolean exists(String memberName) {
        return this.containsKey(memberName);
    }
    
//    @Override
//    public Object put(String key, Object value) {
//        return super.put(key == null ? "null" : key, value);
//    }

    public Json set(Object memberName, Object value) {
        put(String.valueOf(memberName), value);
        return this;
    }

//  get value or array element
    public Object get(String memberName, int... indices) {
        Object obj = get(memberName);
        for (int i = 0; i < indices.length; i++) {
            obj = Array.get(obj, indices[i]);
        }
        return obj;
    }

    public Json getJSON(String memberName, int... indices) {
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
            return (Object[]) JSON.cast(obj, Object[].class);
        }
        return (Object[]) obj; // throws ClassCastException
    }

// casting by sample or class the value of a member or element of an array
    public <T> T castMember(T sample, String memberName, int... indices) {
        return JSON.cast(get(memberName, indices), sample);
    }

    public <T> T castMember(Class<T> cls, String memberName, int... indices) {
        return JSON.cast(get(memberName, indices), cls);
    }

    public Json normalize() throws Exception {
        return (Json) JSON.fromJSON(toString()); // :)
    }

}
