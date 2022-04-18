/**
 * Java JSON parser/generator, MIT (c) 2020-2022 miktim@mail.ru
 *
 * Release notes:
 * - Java 7+, Android compatible;
 * - in accordance with RFC 8259: https://datatracker.ietf.org/doc/rfc8259/?include_text=1
 * - parser converts JSON text to Java objects:
 *   JSON object, String, Number (BigDecimal), Boolean, null, Object[] array of listed types;
 * - JSON object members (name/value pairs) are stored in creation/appearance order;
 * - when the names within an object are not unique, parser stores the last value only;
 * - JSON object setter accepts any Java object, all Java primitives and primitive arrays;
 * - avoid recursion!;
 * - in addition, the generator converts Java Lists, Sets to JSON arrays
 *   and Java Maps to JSON objects. The null key is converted to a "null" member name.
 *   Other Java objects are converted to JSON strings.
 *
 * Created: 2020-03-07
 */
package org.miktim.json;

import java.io.IOException;
import java.io.Reader;
import java.util.LinkedHashMap;
import java.io.StringReader;
import java.io.Writer;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import static java.util.Arrays.binarySearch;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class JSON extends LinkedHashMap<String, Object> {

    public static Object parse(String json) throws IOException, ParseException {
        return parse(new StringReader(json));
    }

    public static Object parse(Reader reader) throws IOException, ParseException {
        return (new Parser()).parse(reader);
    }

    public static String stringify(Object object) {
        return stringifyObject(object);
    }

    public static void stringify(Object object, Writer writer) throws IOException {
        writer.write(stringifyObject(object));
    }

    public JSON(String jsonText) throws IOException, ParseException {
        super();
        this.putAll((JSON) JSON.parse(jsonText));
    }

    public JSON(Reader reader) throws IOException, ParseException {
        super();
        this.putAll((JSON) JSON.parse(reader));
    }

// Memebers: name,value pairs    
    public JSON(Object... members) throws IndexOutOfBoundsException {
        super();
        for (int i = 0; i < members.length;) {
            this.put(String.valueOf(members[i++]), members[i++]);
        }
    }

    public String stringify() {
        return stringify(this);
    }

    public String toString(String memberName, int... indices) {
        return JSON.stringify(get(memberName, indices));
    }

    @Override
    public String toString() {
        return stringify();
    }

    public List<String> listNames() {
        return new ArrayList<>(this.keySet());
    }

    public boolean exists(String memberName) {
        return this.containsKey(memberName);
    }

    public JSON set(String memberName, Object value) {
        this.put(memberName, value);
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

    public <T> T cast(String memberName, T sample, int... indices) {
        return JSONAdapter.cast(get(memberName, indices), sample);
    }

    public <T> T cast(String memberName, Class<T> cls, int... indices) {
        return JSONAdapter.cast(get(memberName, indices), cls);
    }

    public JSON getJSON(String memberName, int... indices) {
        return (JSON) get(memberName, indices);
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
        return (Object[]) get(memberName, indices);
    }

    public JSON normalize() throws Exception {
        return (JSON) JSON.parse(toString()); // :)
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
        private int offset = -1;

        ParseException newParseException(String message, String lexeme, int offset) {
            int off = offset - (lexeme == null ? 0 : lexeme.length());
            String msg = message
                    + (lexeme == null ? "" : " \"" + lexeme + "\"")
                    + " at " + off;
            return new ParseException(msg, off);
        }

        char getChar() throws IOException, ParseException {
            if (eot()) { // end of text
                throw newParseException("Unexpected EOT", null, offset);
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
                        Object memberName = parseObject();
                        if ((memberName instanceof String) && expectedChar(':')) {
                            ((JSON) object).set((String) memberName, parseObject());
                        } else {
                            throw newParseException("Name expected",
                                    null, offset);
                        }
                    } while (expectedChar(','));
                    if (!expectedChar('}')) {
                        throw newParseException("\"}\" expected", null, offset);
                    }
                }
            } else if (expectedChar('[')) { // JSON array
                List<Object> list = new ArrayList<>(); //
                if (!expectedChar(']')) { // empty array
                    do {
                        list.add(parseObject());
                    } while (expectedChar(','));
                    if (!expectedChar(']')) {
                        throw newParseException("\"]\" expected",
                                null, offset);
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
                object = unescapeString(sb.toString()); // may throw ParseException
                getChar(); // skip trailing double quote
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
                        throw newParseException("Unknown literal:",
                                literal, offset);
                }
            } else if (charIn(NUMBERS, lastChar())) {
                String number = nextChars(NUMBERS);
                try {
                    object = new BigDecimal(number);
                } catch (NumberFormatException e) {
                    throw newParseException("Unparseable number:",
                            number, offset);
                }
            } else {
                throw newParseException("Unexpected char:",
                        Character.toString(lastChar()), offset + 1); // !
            }
            skipWhitespaces(); // trailing
            return object;
        }

        Object parse(Reader reader) throws IOException, ParseException {
            this.reader = reader;
            this.lastChar = 0x20; //!!!
            this.offset = -1;
            Object object = parseObject();
            if (!eot()) { // not end of text
                throw newParseException("EOT expected", null, offset);
            }
            return object;
        }
    }

// Java Lists, Sets converts to JSON arrays [V[0],...,V[n]], Maps - to object {"K":V,...}
    @SuppressWarnings("unchecked")
    static String stringifyObject(Object value) {
        if (value == null) {
            return "null";
        } else if (value instanceof String) {
            return "\"" + escapeString((String) value) + "\"";
        } else if (value instanceof Number || value instanceof Boolean) {
            return value.toString(); // Number, Boolean
        } else if (value.getClass().isArray()) {
            StringBuilder sb = new StringBuilder("[");
            String separator = "";
            for (int i = 0; i < Array.getLength(value); i++) {
                sb.append(separator).append(stringifyObject(Array.get(value, i)));
                separator = ", ";
            }
            return sb.append("]").toString();
        } else if (value instanceof Map) {
            StringBuilder sb = new StringBuilder("{");
            String separator = "";
            for (Map.Entry<Object, Object> entry : ((Map<Object, Object>) value).entrySet()) {
                sb.append(separator)
                        // null key (member name) is converted to "null" 
                        .append(stringifyObject(String.valueOf(entry.getKey())))
                        .append(": ")
                        .append(stringifyObject(entry.getValue()));
                separator = ", ";
            }
            return sb.append("}").toString();
        } else if (value instanceof Collection) {
            return stringifyObject(((Collection) value).toArray()); // List, Set, ...
//        } else if (value instanceof List) {
//            return stringifyObject(((List) value).toArray());
//        } else if (value instanceof Set) {
//            return stringifyObject(((Set) value).toArray());
        }
        return stringifyObject(String.valueOf(value));
    }

    private static final char[] ESCAPED_CHARS = {'"', '/', '\\', 'b', 'f', 'n', 'r', 't'};
    private static final char[] CHARS_UNESCAPED = {0x22, 0x2F, 0x5C, 0x8, 0xC, 0xA, 0xD, 0x9};

    public static String unescapeString(String s) throws ParseException {
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
                    try {
                        sb.append((char) Integer.parseInt(
                                new String(chars, i + 1, 4), 16));
                    } catch (NumberFormatException | IndexOutOfBoundsException e) {
//                        sb.append("\\u"); // ignore unparseable u-escaped char
//                        continue;
                        throw new ParseException("Unparseable u-escaped char in: \""
                                + s + "\" at " + --i, i);
                    }
                    i += 4;
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
