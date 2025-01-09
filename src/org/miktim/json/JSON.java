/**
 * Java JSON parser/generator, MIT (c) 2020-2025 miktim@mail.ru
 *
 * Release notes:
 * - Java 7+, Android compatible;
 * - in accordance with RFC 8259: https://datatracker.ietf.org/doc/rfc8259/?include_text=1
 * - parser converts JSON text to Java objects:
 *   Json object, String, Number, Boolean, null, Object[] array of listed types;
 * - when the names within an object are not unique, parser stores the last value only;
 * - other Java objects are converted by default.
 *
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
import java.lang.reflect.Constructor;
import java.text.ParseException;

public class JSON {

    public static <T> T toJSON(T obj, OutputStream out, int space, String charsetName)
            throws IOException {
        JSONGenerator generator = new JSONGenerator(out, space, charsetName);
        generator.generateObject(obj, 0);
        return obj;
    }

    public static <T> T toJSON(T obj, OutputStream out, int space)
            throws IOException {
        JSONGenerator generator = new JSONGenerator(out, space, "UTF-8");
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
        JSONParser parser = new JSONParser(in, charsetName);
        return parser.parseObject();
    }

    public static Object fromJSON(String jsonText)
            throws IOException, ParseException {
        ByteArrayInputStream in
                = new ByteArrayInputStream(jsonText.getBytes("UTF-8"));
        return fromJSON(in, "UTF-8");
    }

    @SuppressWarnings("unchecked")
    public static <T> T cast(T sample, Object jsonVal) {
        if (sample == null) {
            return null;
        }
        return (T) cast(sample.getClass(), jsonVal);
    }

    @SuppressWarnings("unchecked")
    public static <T> T cast(Class<T> dstCls, Object obj) {
        if (dstCls == null) {
            return null;
        }

        CastAdapter adapter = getAdapter(getElementClass(dstCls));
//        if(obj == null) return (T) adapter.castValue(obj);
        return (T) cast(dstCls, obj, adapter);
    }

    @SuppressWarnings({"unchecked"})
    static <T> T cast(Class<T> dstCls, Object obj, CastAdapter adapter) {
        if (dstCls.isArray()) {
            int arrLen = obj == null ? 0 : Array.getLength(obj);
            Class cmpDstCls = dstCls.getComponentType();
            T arr = (T) Array.newInstance(cmpDstCls, arrLen);
            for (int i = 0; i < arrLen; i++) {
                T retVal = (T) cast(cmpDstCls, Array.get(obj, i), adapter);
                Array.set(arr, i, retVal);
            }

            return (T) arr;
        }
        return (T) adapter.castValue(obj);
    }

    public static Class getElementClass(Class cls) {
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

    public static boolean isNativeType(Object obj) {
        if (obj == null) {
            return true;
        }
        return isNativeClass(obj.getClass());
    }
    
    public static boolean isNativeClass(Class clazz) {
        Class<?> cls = JSON.getElementClass(clazz);
        return cls.equals(Number.class)
                || cls.equals(String.class)
                || cls.equals(Boolean.class)
                || cls.equals(Character.class)
                || cls.equals(Json.class)
                || cls.getSuperclass().equals(Number.class);
    }

    interface CastAdapter {

        <T> T castValue(Object value) throws ClassCastException;
    }
    
// TODO: ?safeCast from BigDecimal
    @SuppressWarnings("unchecked")
    static CastAdapter getAdapter(Class cls) {
        // find an adapter 
        if (cls == Integer.class) {
            return intAdapter;
        } else if (cls == Long.class) {
            return longAdapter;
        } else if (cls == Double.class) {
            return doubleAdapter;
        } else if (cls == Float.class) {
            return floatAdapter;
        } else if (cls == String.class) {
            return stringAdapter;
        } else if (cls == Byte.class) {
            return byteAdapter;
        } else if (cls == Short.class) {
            return shortAdapter;
        } else if (cls == Character.class) {
            return charAdapter;
        } else if (cls == Boolean.class) {
            return booleanAdapter;
        } else if (cls == Json.class) {
            return jsonAdapter;
//        } else if (JsonConvertible.class.isAssignableFrom(cls)) {
//            return new ObjectAdapter(cls);
        }
        return new ObjectAdapter(cls);
    }

    @SuppressWarnings("unchecked")
    static class ObjectAdapter implements CastAdapter {

        Class<?> clazz;

        ObjectAdapter(Class<?> clazz) {
            this.clazz = clazz;
        }

        @Override
        public Object castValue(Object obj) {
            try {
                if (obj == null || obj.getClass() == clazz) {
                    return obj;
                }
//                if(obj.getClass() == clazz) return obj;
                Object newObj = createInstance(clazz);
                return Json.converter.fromJson(newObj, (Json)obj);
            } catch (IllegalArgumentException ex) {
                throw new ClassCastException(ex.getMessage());
            }
        }
    };

// https://stackoverflow.com/questions/6094575/creating-an-instance-using-the-class-name-and-calling-constructor
    static Object createInstance(Class<?> clazz) {
        try {
//            Class<?> clazz = obj.getClass();
            Constructor<?> ctor = clazz.getConstructor();
            return ctor.newInstance();//new Object[0]);
        } catch (Exception ex) {
            throw new ClassCastException(ex.getMessage());
        }
    }
    @SuppressWarnings("unchecked")
    private static final CastAdapter jsonAdapter = new CastAdapter() {
        @Override
        public Object castValue(Object obj) {
            return obj == null ? new Json() : (Json) obj;
        }
    };
    @SuppressWarnings("unchecked")
    private static final CastAdapter booleanAdapter = new CastAdapter() {
        @Override
        public Object castValue(Object obj) {
            return new Boolean(String.valueOf(obj));
        }
    };
    @SuppressWarnings("unchecked")
    private static final CastAdapter intAdapter = new CastAdapter() {
        @Override
        public Object castValue(Object obj) {
            return obj == null ? 0 : ((Number) obj).intValue();
        }
    };
    @SuppressWarnings("unchecked")
    private static final CastAdapter byteAdapter = new CastAdapter() {
        @Override
        public Object castValue(Object obj) {
            return obj == null ? 0 : ((Number) obj).byteValue();
        }
    };
    @SuppressWarnings("unchecked")
    private static final CastAdapter shortAdapter = new CastAdapter() {
        @Override
        public Object castValue(Object obj) {
            return obj == null ? 0 : ((Number) obj).shortValue();
        }
    };
    @SuppressWarnings("unchecked")
    private static final CastAdapter longAdapter = new CastAdapter() {
        @Override
        public Object castValue(Object obj) {
            return obj == null ? 0L : ((Number) obj).longValue();
        }
    };
    @SuppressWarnings("unchecked")
    private static final CastAdapter floatAdapter = new CastAdapter() {
        @Override
        public Object castValue(Object obj) {
            return obj == null ? 0f : ((Number) obj).floatValue();
        }
    };
    @SuppressWarnings("unchecked")
    private static final CastAdapter doubleAdapter = new CastAdapter() {
        @Override
        public Object castValue(Object obj) {
            return obj == null ? 0 : ((Number) obj).doubleValue();
        }
    };
    @SuppressWarnings("unchecked")
    private static final CastAdapter charAdapter = new CastAdapter() {
        @Override
        public Object castValue(Object obj) {
            return obj == null ? (char) 0
                    : (obj instanceof Character ? obj : (((String) obj) + "\u0000").charAt(0));
        }
    };
    @SuppressWarnings("unchecked")
    private static final CastAdapter stringAdapter = new CastAdapter() {
        @Override
        public String castValue(Object obj) {
            return obj == null ? null : String.valueOf(obj);
        }
    };
}
