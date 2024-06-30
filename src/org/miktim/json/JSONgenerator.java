/**
 * JSON generator, MIT (c) 2020-2024 miktim@mail.ru
 *
 * Release notes:
 * - parser converts JSON text to Java objects:
 *   Json object, String, Number (Double or Long), Boolean, null, Object[] array of listed types;
 * - in addition to listed types, the generator converts:
 *     Java Collections to JSON arrays;
 *     Java Maps to Json objects. The null key is converted to a "null" member name;
 *     Other Java objects are converted to string representation.
 */

package org.miktim.json;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import static org.miktim.json.JSON.escapeString;

class JSONgenerator {

    int intend = 0;
    String charsetName = "UTF-8";
    OutputStream stream;
    byte[] newLine = "\n".getBytes();

    JSONgenerator(OutputStream outStream, int space, String charsetName) {
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
        if (value == null || value instanceof Number || value instanceof Boolean) {
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
        } else if (value instanceof Map) {
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
        generateObject(String.valueOf(value), level);
    }
}
