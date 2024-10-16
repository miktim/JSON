/**
 * JSON generator, MIT (c) 2020-2024 miktim@mail.ru
 *
 * Generator converts to JSON Java objects:
 * - Json object, String, Number, Boolean, null, Object[] array of listed types;
 * - Java primitives and arrays of primitives;
 * - Java Collections to JSON arrays and Java Maps to Json objects.
 *   The null key is converted to a "null" member name;
 * - Other Java objects are converted to string representation.
 */
package org.miktim.json;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.util.Arrays;
import static java.util.Arrays.binarySearch;
import java.util.Collection;
import java.util.Map;

class JSONGenerator {

    int intend = 0;
    String charsetName = "UTF-8";
    OutputStream stream;
    byte[] newLine = "\n".getBytes();

    JSONGenerator(OutputStream outStream, int space, String charsetName) {
        intend = space;
        this.charsetName = charsetName;
        stream = outStream;
    }

    void write(Object obj) throws UnsupportedEncodingException, IOException {
        stream.write(String.valueOf(obj).getBytes(this.charsetName));
    }

    void write(Object obj, int level) throws IOException {
        write(obj);
        if (intend > 0) {
            stream.write(newLine);
            if (level > 0) {
                byte[] offsetBytes = new byte[intend * level];
                Arrays.fill(offsetBytes, (byte) ' ');
                stream.write(offsetBytes);
            }
        }
    }

// Java Lists, Sets converts to JSON arrays [V[0],...,V[n]], Maps - to object {"K":V,...}
    @SuppressWarnings("unchecked")
    void generateObject(Object value, int level) throws IOException {
        level++;
        if (value == null || value instanceof Number
                || value instanceof Boolean) {
            write(value);
            return;
        } else if (value instanceof String) {
            write("\"" + escapeString((String) value) + "\"");
            return;
        } else if (value.getClass().isArray()) {
            write("[");
            String separator = "";
            for (int i = 0; i < Array.getLength(value); i++) {
                write(separator, level);
                generateObject(Array.get(value, i), level);
                separator = ", ";
            }
            write("", level - 1);
            write("]");
            return;
        } else if (value instanceof Map) { //Json) {
            write("{");
            String separator = "";
            for (Map.Entry<Object, Object> entry : ((Map<Object, Object>) value).entrySet()) {
                write(separator, level);
                // null key (member name) is converted to "null" 
                generateObject(String.valueOf(entry.getKey()), level);
                write(": ");
                generateObject(entry.getValue(), level);
                separator = ", ";
            }
            write("", level - 1);
            write("}");
            return;
        } else if (value instanceof Collection) {
            generateObject(((Collection) value).toArray(), level);
            return;
        }
//        throw new RuntimeException("Unsupported type: " + value.getClass().getName());
        generateObject(String.valueOf(value), level);
    }

    private static final int[] UNESCAPED_CHARS = {0x8, 0x9, 0xA, 0xC, 0xD, 0x22, 0x2F, 0x5C};
    private static final String[] CHARS_ESCAPED = {"\\b", "\\t", "\\n", "\\f", "\\r", "\\\"", "\\/", "\\\\"};

    static String escapeString(String s) {
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

}
