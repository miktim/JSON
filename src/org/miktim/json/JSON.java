/**
 * Java JSON parser/generator, MIT (c) 2020-2024 miktim@mail.ru
 *
 * Release notes:
 * - Java 7+, Android compatible;
 * - in accordance with RFC 8259: https://datatracker.ietf.org/doc/rfc8259/?include_text=1
 * - parser converts JSON text to Java objects:
 *   Json object, String, Number (Double or Long), Boolean, null, Object[] array of listed types;
 * - JSON object members (name/value pairs) are stored in creation/appearance order;
 * - when the names within an object are not unique, parser stores the last value only;
 * - in addition, the generator converts Java Collections to JSON arrays
 *   and Java Maps to Json objects. The null key is converted to a "null" member name.
 *   Other Java objects are converted to string representation.
 */

package org.miktim.json;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.text.ParseException;
import static java.util.Arrays.binarySearch;

public abstract class JSON {

    public static <T>T toJSON(T obj, OutputStream out, int space, String charsetName)
            throws IOException {
        JSONgenerator generator = new JSONgenerator(out, space, charsetName);
        generator.generateObject(obj, 0);
        return obj;
    }

    public static <T>T toJSON(T obj, OutputStream out)
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
        JSONparser parser = new JSONparser(in, charsetName);
        return parser.parseObject();
    }

    public static Object fromJSON(String jsonText) 
            throws IOException, ParseException {
        ByteArrayInputStream in = 
                new ByteArrayInputStream(jsonText.getBytes("UTF-8"));
        return fromJSON(in, "UTF-8");
    }

    public static Number toNumber(String number)
            throws NumberFormatException {
//          return new BigDecimal(number);
        if (number.indexOf('.') >= 0 || number.indexOf('E') >= 0 || number.indexOf('e') >= 0) {
            return Double.parseDouble(number);
        }
        return Long.parseLong(number);
    }

    private static final char[] ESCAPED_CHARS = {'"', '/', '\\', 'b', 'f', 'n', 'r', 't'};
    private static final char[] CHARS_UNESCAPED = {0x22, 0x2F, 0x5C, 0x8, 0xC, 0xA, 0xD, 0x9};

    public static String unescapeString(String s) throws ParseException {
        StringBuilder sb = new StringBuilder(128);
        char[] chars = s.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (c == '\\') {
                c = chars[++i];
                if (c == 'u') {
                    try {
                        c = ((char) Integer.parseInt(
                                new String(chars, i + 1, 4), 16));
                        i += 4;
                    } catch (NumberFormatException | IndexOutOfBoundsException e) {
                        throw new ParseException("Unparseable u-escaped char in: \""
                                + s + "\" at " + --i, i);
                    }
                } else {
                    int ei = binarySearch(ESCAPED_CHARS, c);
                    if (ei >= 0) {
                        c = (CHARS_UNESCAPED[ei]);
                    } else {
                        throw new ParseException("Wrong two-character escape in: \""
                                + s + "\" at " + --i, i);
                    }
                }
            } else if (c < 0x20) {
                throw new ParseException("Unescaped control char in: \""
                        + s + "\" at " + i, i);
            }
            sb.append(c);
        }
        return sb.toString();
    }

    private static final int[] UNESCAPED_CHARS = {0x8, 0x9, 0xA, 0xC, 0xD, 0x22, 0x5C}; // {0x8, 0x9, 0xA, 0xC, 0xD, 0x22, 0x2F, 0x5C}
    private static final String[] CHARS_ESCAPED = {"\\b", "\\t", "\\n", "\\f", "\\r", "\\\"", "\\\\"}; // {"\\b", "\\t", "\\n", "\\f", "\\r", "\\\"", "\\/", "\\\\"}

    public static String escapeString(String s) {
        StringBuilder sb = new StringBuilder(64);
        for (int i = 0; i < s.length(); i++) {
            int c = s.codePointAt(i);
            int ei = binarySearch(UNESCAPED_CHARS, c);
            if (ei >= 0) {
                sb.append(CHARS_ESCAPED[ei]);
            } else if (c < 0x20) {
                sb.append(String.format("\\u%04X", c));
            } else if (c > 0xFFFF) {
                c -= 0x10000;
                sb.append(String.format("\\u%04X\\u%04X",
                        (c >>> 10) + 0xD800, (c & 0x3FF) + 0xDC00)); // surrogates
                i++;
            } else {
                sb.append((char) c);
            }
        }
        return sb.toString();
    }
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
