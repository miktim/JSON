/**
 * Java JSON parser/generator, MIT (c) 2020-2024 miktim@mail.ru
 *
 * Release notes:
 * - Java 7+, Android compatible;
 * - in accordance with RFC 8259: https://datatracker.ietf.org/doc/rfc8259/?include_text=1
 * - parser converts JSON text to Java objects:
 *   Json object, String, Number, Boolean, null, Object[] array of listed types;
 * - JSON object members (name/value pairs) are stored in creation/appearance order;
 * - when the names within an object are not unique, parser stores the last value only;
 * - in addition, the generator converts Java Collections to JSON arrays
 *   and Java Maps to Json objects. The null key is converted to a "null" member name.
 *   Other Java objects are converted to string representation.
 * Reasons for using the Object[]:
 * - JSON allows mixed-type arrays;
 * - JSON empty array has unknown Java type.
 */
package org.miktim.json;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.text.ParseException;

public abstract class JSON {
/*
    static {
        try {

        } catch (Exception e) {

        }
    }

    private static LinkedHashMap<String, JsonObject> objects
            = new LinkedHashMap<>();

    public static void registerJsonObject(Object obj) {
        if (obj instanceof JsonObject) {
            registerJsonObject((JsonObject) obj, obj.getClass().getName());
        }
    }

    public static void registerJsonObject(JsonObject obj, String className) {
        objects.put(className, obj);
    }
*/
    public static <T> T toJSON(T obj, OutputStream out, int space, String charsetName)
            throws IOException {
        GeneratorJSON generator = new GeneratorJSON(out, space, charsetName);
        generator.generateObject(obj, 0);
        return obj;
    }

    public static <T> T toJSON(T obj, OutputStream out)
            throws IOException {
        return toJSON(obj, out, 0, "UTF-8");
    }

    public static String toJSON(Object obj, int space) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        toJSON(obj, stream, space, "UTF-8");
        return stream.toString("UTF-8");
    }

    public static String toJSON(Object obj) throws IOException {
        return toJSON(obj, 0);
    }

    public static Object fromJSON(InputStream in, String charsetName)
            throws IOException, ParseException {
        ParserJSON parser = new ParserJSON(in, charsetName);
        return parser.parseObject();
    }

    public static Object fromJSON(String jsonText)
            throws IOException, ParseException {
        ByteArrayInputStream in
                = new ByteArrayInputStream(jsonText.getBytes("UTF-8"));
        return fromJSON(in, "UTF-8");
    }
/*
    public static boolean isMemberType(Object obj) {
        if (obj == null) {
            return true;
        }
        Class<?> c = obj.getClass();
        return isBasicClass(c)
                || c.isArray();
    }

    public static boolean isBasicArrayType(Object obj) {
        if (obj == null) {
            return false;
        }
        Class<?> c = obj.getClass();
        for (; c.isArray(); c = c.getComponentType()) {
            if (!(c.isArray() || isBasicClass(c))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isBasicType(Object obj) {
        if (obj == null) {
            return true;
        }
        Class<?> c = obj.getClass();
        return isBasicClass(c);
    }

    public static boolean isBasicClass(Class<?> c) {
        return c.getSuperclass().equals(Number.class)
                || c.equals(String.class)
                || c.equals(Boolean.class)
                || c.equals(Json.class);
    }
*/
//public class JsonAdapter {
    @SuppressWarnings("unchecked")
    public static <T> T cast(Object jsonVal, T sample) {
        if (sample == null) {
            return null;
        }
        return (T) cast(jsonVal, sample.getClass());
    }

    @SuppressWarnings("unchecked")
    public static <T> T cast(Object jsonVal, Class<T> cls) {
        if (cls == null) {
            return null;
        }
        Adapter adapter = getAdapter(getElementClass(cls));
        return (T) cast(jsonVal, cls, adapter);
    }

    public interface Adapter {

        <T> T fromJson(Object jsonVal);

        <T> T toJson(Object val);
    }

    @SuppressWarnings("unchecked")
    private static <T> T cast(Object jsonObj, Class<T> cls, Adapter adapter) {
        if (cls.isArray()) {
            int objLen = jsonObj == null ? 0 : Array.getLength(jsonObj);
            Class cmpCls = cls.getComponentType();
            T arr = (T) Array.newInstance(cmpCls, objLen);
            for (int i = 0; i < objLen; i++) {
                Array.set(arr, i, cast(Array.get(jsonObj, i), cmpCls, adapter));
            }
            return (T) arr;
        }

        return adapter.fromJson(jsonObj);
    }

    private static Class getElementClass(Class cls) {
        while (cls.isArray()) {
            cls = cls.getComponentType();
        }
// cast primitive to corresponding object
        Object sample = Array.get(Array.newInstance(cls, 1), 0);
        if (sample != null) {
            cls = sample.getClass();
        }
        return cls;
    }

    @SuppressWarnings("unchecked")
    private static Adapter getAdapter(Class cls) {
        if (cls == Integer.class) {
            return intAdapter;
        } else if (cls == Long.class) {
            return longAdapter;
        } else if (cls == Double.class) {
            return doubleAdapter;
        } else if (cls == Float.class) {
            return floatAdapter;
        } else if (cls == Byte.class) {
            return byteAdapter;
        } else if (cls == Short.class) {
            return shortAdapter;
        } else if (cls == Character.class) {
            return charAdapter;
        } else if (cls == Boolean.class) {
            return booleanAdapter;
        } else if (cls == String.class) {
            return stringAdapter;
        }
        return defaultAdapter;
    }
    @SuppressWarnings("unchecked")
    private static final Adapter defaultAdapter = new Adapter() {
        @Override
        public Object fromJson(Object obj) {
            return obj;
        }

        @Override
        public Object toJson(Object obj) {
            return obj;
        }
    };
    @SuppressWarnings("unchecked")
    private static final Adapter booleanAdapter = new Adapter() {
        @Override
        public Object fromJson(Object obj) {
            return obj == null ? false : obj;
        }

        @Override
        public Object toJson(Object obj) {
            return obj;
        }
    };
    @SuppressWarnings("unchecked")
    private static final Adapter charAdapter = new Adapter() {
        @Override
        public Object fromJson(Object obj) {
            return obj == null ? (char) 0
                    : (obj instanceof Character ? obj : (((String) obj) + "\u0000").charAt(0));
        }

        @Override
        public Object toJson(Object obj) {
            return obj;
        }
    };
    @SuppressWarnings("unchecked")
    private static final Adapter intAdapter = new Adapter() {
        @Override
        public Object fromJson(Object obj) {
            return obj == null ? 0 : ((Number) obj).intValue();
        }

        @Override
        public Object toJson(Object obj) {
            return obj;
        }
    };
    @SuppressWarnings("unchecked")
    private static final Adapter byteAdapter = new Adapter() {
        @Override
        public Object fromJson(Object obj) {
            return obj == null ? 0 : ((Number) obj).byteValue();
        }

        @Override
        public Object toJson(Object obj) {
            return obj;
        }
    };
    @SuppressWarnings("unchecked")
    private static final Adapter shortAdapter = new Adapter() {
        @Override
        public Object fromJson(Object obj) {
            return obj == null ? 0 : ((Number) obj).shortValue();
        }

        @Override
        public Object toJson(Object obj) {
            return obj;
        }
    };
    @SuppressWarnings("unchecked")
    private static final Adapter longAdapter = new Adapter() {
        @Override
        public Object fromJson(Object obj) {
            return obj == null ? 0 : ((Number) obj).longValue();
        }

        @Override
        public Object toJson(Object obj) {
            return obj;
        }
    };
    @SuppressWarnings("unchecked")
    private static final Adapter floatAdapter = new Adapter() {
        @Override
        public Object fromJson(Object obj) {
            return obj == null ? 0 : ((Number) obj).floatValue();
        }

        @Override
        public Object toJson(Object obj) {
            return obj;
        }
    };
    @SuppressWarnings("unchecked")
    private static final Adapter doubleAdapter = new Adapter() {
        @Override
        public Object fromJson(Object obj) {
            return obj == null ? 0 : ((Number) obj).doubleValue();
        }

        @Override
        public Object toJson(Object obj) {
            return obj;
        }
    };
    @SuppressWarnings("unchecked")
    private static final Adapter stringAdapter = new Adapter() {
        @Override
        public String fromJson(Object obj) {
            return String.valueOf(obj);
        }

        @Override
        public Object toJson(Object obj) {
            return obj;
        }
    };
}
