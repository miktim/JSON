/*
 * JSONAdapter class. MIT (c) 2022 miktim@mail.ru
 * Cast by sample/class JSON variable to Java primitive or primitive array
 */
package org.miktim.json;

import java.lang.reflect.Array;

public class JSONAdapter {

    @SuppressWarnings("unchecked")
    public static <T> T cast(Object jsonVal, T sample) {
        if (sample == null) {
            return null;
        }
        return (T) JSONAdapter.cast(jsonVal, sample.getClass());
    }

    @SuppressWarnings("unchecked")
    public static <T> T cast(Object jsonVal, Class<T> cls) {
        if (cls == null) {
            return null;
        }
        Adapter adapter = getAdapter(getElementClass(cls));
        return (T) JSONAdapter.cast(jsonVal, cls, adapter);
    }

    private interface Adapter {

        <T> T castValue(Object jsonVal);
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

        return adapter.castValue(jsonObj);
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
        public Object castValue(Object obj) {
            return obj;
        }
    };
    @SuppressWarnings("unchecked")
    private static final Adapter booleanAdapter = new Adapter() {
        @Override
        public Object castValue(Object obj) {
            return obj == null ? false : obj;
        }
    };
    @SuppressWarnings("unchecked")
    private static final Adapter intAdapter = new Adapter() {
        @Override
        public Object castValue(Object obj) {
            return obj == null ? 0 : ((Number) obj).intValue();
        }
    };
    @SuppressWarnings("unchecked")
    private static final Adapter byteAdapter = new Adapter() {
        @Override
        public Object castValue(Object obj) {
            return obj == null ? 0 : ((Number) obj).byteValue();
        }
    };
    @SuppressWarnings("unchecked")
    private static final Adapter shortAdapter = new Adapter() {
        @Override
        public Object castValue(Object obj) {
            return obj == null ? 0 : ((Number) obj).shortValue();
        }
    };
    @SuppressWarnings("unchecked")
    private static final Adapter longAdapter = new Adapter() {
        @Override
        public Object castValue(Object obj) {
            return obj == null ? 0 : ((Number) obj).longValue();
        }
    };
    @SuppressWarnings("unchecked")
    private static final Adapter floatAdapter = new Adapter() {
        @Override
        public Object castValue(Object obj) {
            return obj == null ? 0 : ((Number) obj).floatValue();
        }
    };
    @SuppressWarnings("unchecked")
    private static final Adapter doubleAdapter = new Adapter() {
        @Override
        public Object castValue(Object obj) {
            return obj == null ? 0 : ((Number) obj).doubleValue();
        }
    };
    @SuppressWarnings("unchecked")
    private static final Adapter charAdapter = new Adapter() {
        @Override
        public Object castValue(Object obj) {
            return obj == null ? (char) 0
                    : (obj instanceof Character ? obj : (((String) obj)+"\u0000").charAt(0));
        }
    };
    @SuppressWarnings("unchecked")
    private static final Adapter stringAdapter = new Adapter() {
        @Override
        public String castValue(Object obj) {
            return String.valueOf(obj);
        }
    };

}
