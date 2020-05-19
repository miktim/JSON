/**
 * Java JSON parser/serializer, MIT (c) 2020 miktim@mail.ru
 *
 * Release notes:
 * - java 1.7+, Android compatible;
 * - in accordance with RFC 8259: https://datatracker.ietf.org/doc/rfc8259/?include_text=1
 * - supported java objects:
 *   JSON object, String, Number, Boolean, null, Object[] array of listed types;
 * - parser implements BigDecimal for numbers.
 *
 * Created: 2020-03-07
 */
package org.samples.java;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import java.util.LinkedHashMap;
import java.io.StringReader;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import static java.util.Arrays.binarySearch;
import java.util.List;
import java.util.Vector; //obsolete?

public class JSON implements Cloneable {

    public static Object parse(String json) throws IOException, ParseException {
        return parse(new StringReader(json));
    }

    public static Object parse(Reader reader) throws IOException, ParseException {
        return (new Parser()).parse(reader);
    }

    public static String stringify(Object object) throws IllegalArgumentException {
        return stringifyObject(checkObjectType(object));
    }

    private final LinkedHashMap<String, Object> properties = new LinkedHashMap<>();

    public JSON() {
    }

    public String stringify() {
        return stringifyObject(this);
    }

    @Override
    public String toString() {
        return this.stringify();
    }

    @Override
    public JSON clone() throws CloneNotSupportedException {
        super.clone();
        try {
            return (JSON) JSON.parse(this.stringify());
        } catch (Exception e) {
            throw new CloneNotSupportedException();
        }
    }

    public List<String> list() {
        return new ArrayList<>(this.properties.keySet());
    }

    public boolean exists(String propName) {
        return listProperties().containsKey(propName);
    }

    public Object get(String propName) throws IllegalArgumentException {
        if (propName == null || propName.isEmpty() || !exists(propName)) {
            throw new IllegalArgumentException();
        }
        return listProperties().get(propName);
    }

    public JSON set(String propName, Object value) throws IllegalArgumentException {
        if (propName == null || propName.isEmpty()) {
            throw new IllegalArgumentException();
        }
        listProperties().put(propName, checkObjectType(value));
        return this;
    }

    public Object remove(String propName) {
        return this.listProperties().remove(propName);
    }

    private LinkedHashMap<String, Object> listProperties() {
        return this.properties;
    }

    static class Parser {

        private static final char[] WHITESPACES = " \n\r\t".toCharArray();
        private static final char[] NUMBERS = "+-0123456789eE.".toCharArray();
        private static final char[] LITERALS = "truefalsn".toCharArray();

        static {
            Arrays.sort(WHITESPACES);
            Arrays.sort(NUMBERS);
            Arrays.sort(LITERALS);
        }

        private Reader reader;
        private int lastChar = 0x20;
        private int offset = 0;

        char getChar() throws IOException, ParseException {
            if (eot()) { // end of text
                throw new ParseException("Unexpected EOT", offset);
            }
            this.lastChar = this.reader.read();
            this.offset++;
            return this.lastChar();
        }

        char lastChar() {
            return (char) this.lastChar;
        }

        boolean charIn(char[] chars, char key) {
            return binarySearch(chars, key) >= 0;
        }

        boolean eot() {// end of text?
            return this.lastChar == -1;
        }

        String nextChars(char[] chars) throws IOException, ParseException {
            StringBuilder sb = new StringBuilder(64); // ???CharBuffer
            while (charIn(chars, lastChar())) {
                sb.append(Character.toString(lastChar()));
                getChar();
            }
            return sb.toString();
        }

        char skipWhitespaces() throws IOException, ParseException {
            nextChars(WHITESPACES);
            return lastChar();
        }

        boolean expectedChar(char echar) throws IOException, ParseException {
            if (skipWhitespaces() == echar) {
                getChar(); // skip expected char
                return true;
            }
            return false;
        }

        private Object parseObject() throws IOException, ParseException {
//            skipWhitespaces(); // leading
            Object object = null;
            if (expectedChar('{')) { // JSON object
                object = new JSON();
                if (!expectedChar('}')) { // empty object
                    do {
                        Object propName = parseObject();
                        if ((propName instanceof String) && expectedChar(':')) {
                            ((JSON) object).set((String) propName, parseObject());
                        } else {
                            throw new ParseException("Property name expected",
                                    offset);
                        }
                    } while (expectedChar(','));
                    if (!expectedChar('}')) {
                        throw new ParseException("'}' expected", offset);
                    }
                }
            } else if (expectedChar('[')) { // JSON array
                Vector<Object> list = new Vector<>(); //obsolete?
                if (!expectedChar(']')) { // empty array
                    do {
                        list.add(parseObject());
                    } while (expectedChar(','));
                    if (!expectedChar(']')) {
                        throw new ParseException("']' expected", offset);
                    }
                }
                object = list.toArray();
            } else if (lastChar() == '"') { // String
                StringBuilder sb = new StringBuilder(128); // ???CharBuffer
                while (getChar() != '"') {
                    sb.append(lastChar());
                    if (lastChar() == '\\') {
                        sb.append(getChar());
                    }
                }
                getChar(); // skip trailing double quote
                object = unescapeString(sb.toString());
            } else if (charIn(LITERALS, lastChar())) {
                String literal = nextChars(LITERALS);
                switch (literal) {
                    case "true":
                        object = (Boolean) true;
                        break;
                    case "false":
                        object = (Boolean) false;
                        break;
                    case "null":
                        object = null;
                        break;
                    default:
                        throw new ParseException("Unknown literal: " + literal,
                                offset - literal.length());
                }
            } else if (charIn(NUMBERS, lastChar())) {
                String number = nextChars(NUMBERS);
                try {
                    object = (Number) new BigDecimal(number);
                } catch (NumberFormatException e) {
                    throw new ParseException("Unparseable number: " + number,
                            offset - number.length());
                }
            } else {
                throw new ParseException("Unexpected char: " + lastChar(),
                        offset - 1);
            }
            skipWhitespaces(); // trailing
            return object;
        }

        Object parse(Reader reader) throws IOException, ParseException {
            this.reader = reader;
            this.lastChar = 0x20; //!!!
            this.offset = 0;
            Object object = parseObject();
            if (!eot()) { // not end of text
                throw new ParseException("EOT expected", offset);
            }
            return object;
        }
    }

    static String stringifyObject(Object value) {
        if (value == null) {
            return "null";
        } else if (value instanceof JSON) {
            LinkedHashMap<String, Object> hmap
                    = ((JSON) value).listProperties();
            StringBuilder sb = new StringBuilder("{");
            String delimiter = "";
            for (Map.Entry<String, Object> entry : hmap.entrySet()) {
                sb.append(delimiter)
                        .append(stringifyObject(entry.getKey()))
                        .append(":")
                        .append(stringifyObject(entry.getValue()));
                delimiter = ",";
            }
            return sb.append("}").toString();
        } else if (value instanceof Object[]) {
            StringBuilder sb = new StringBuilder("[");
            String delimiter = "";
            for (Object object : (Object[]) value) {
                sb.append(delimiter).append(stringifyObject(object));
                delimiter = ",";
            }
            return sb.append("]").toString();
        } else if (value instanceof String) {
            return "\"" + escapeString((String) value) + "\"";
        }
        return value.toString(); // Number, Boolean
    }

    static Object checkObjectType(Object object) throws IllegalArgumentException {
        if (object == null
                || (object instanceof String)
                || (object instanceof Number)
                || (object instanceof Boolean)
                || (object instanceof JSON)) {
            return object;
        } else if (object instanceof Object[]) {
            for (Object entry : (Object[]) object) {
                checkObjectType(entry);
            }
            return object;
        }
        throw new IllegalArgumentException();
    }

    private static final char[] ESCAPED_CHARS = {'"', '/', '\\', 'b', 'f', 'n', 'r', 't'};
    private static final char[] CHARS_UNESCAPED = {0x22, 0x2F, 0x5C, 0x8, 0xC, 0xA, 0xD, 0x9};

    public static String unescapeString(String s) {
        StringBuilder sb = new StringBuilder(64);
        char[] chars = s.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (c == '\\') {
                c = chars[++i];
                int ei = binarySearch(ESCAPED_CHARS, c);
                if (ei >= 0) {
                    sb.append(CHARS_UNESCAPED[ei]);
                    continue;
                } else if (c == 'u') {
                    sb.append((char) Integer.parseInt(new String(chars, ++i, 4), 16));
                    i += 3;
                    continue;
                }
            }
            sb.append(c);
        }
        return sb.toString();
    }

    private static final int[] UNESCAPED_CHARS = {0x8, 0x9, 0xA, 0xC, 0xD, 0x22, 0x2F, 0x5C}; //
    private static final String[] CHARS_ESCAPED = {"\\b", "\\t", "\\n", "\\f", "\\r", "\\\"", "\\/", "\\\\"};

    public static String escapeString(String s) {
        StringBuilder sb = new StringBuilder(64);
        for (int i = 0; i < s.length(); i++) {
            int c = s.codePointAt(i);
            int ei = binarySearch(UNESCAPED_CHARS, c);
            if (ei >= 0) {
                sb.append(CHARS_ESCAPED[ei]);
            } else if (c <= 0x1F) {
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
