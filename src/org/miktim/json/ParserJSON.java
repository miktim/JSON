/**
 * JSON parser, MIT (c) 2020 -2024 miktim@mail.ru
 *
 * Release notes:
 * - parser converts JSON text to Java objects:
 *   Json object, String, Number (Double or Long), Boolean, null, Object[] array of listed types;
 * - JSON object members (name/value pairs) are stored in creation/appearance order;
 * - when the names within an object are not unique, parser stores the last value only;
 */
package org.miktim.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import static java.util.Arrays.binarySearch;
import java.util.List;

class ParserJSON {

    String charsetName;
    InputStream stream;

    ParserJSON(InputStream inStream, String charsetName) throws UnsupportedEncodingException {
        this.charsetName = charsetName;
        stream = inStream;
        reader = new InputStreamReader(inStream, charsetName);
    }
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

    String errorChar() {
        return String.valueOf(lastChar);
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

    Object parseObject() throws IOException, ParseException {
//            skipWhitespaces(); // leading
        Object object = null;
        if (expectedChar('{')) { // JSON object
            object = new Json();
            if (!expectedChar('}')) { // empty object
                do {
                    Object memberName = parseObject();
                    if ((memberName instanceof String) && expectedChar(':')) {
                        // superPut without checking value
                        ((Json) object).superPut((String) memberName, parseObject());
                    } else {
                        throw new ParseException("Name expected", offset);
                    }
                } while (expectedChar(','));
                if (!expectedChar('}')) {
                    throw new ParseException("\"}\" expected", offset);
                }
            }
        } else if (expectedChar('[')) { // JSON array
            List<Object> list = new ArrayList<Object>(); //
            if (!expectedChar(']')) { // empty array
                do {
                    list.add(parseObject());
                } while (expectedChar(','));
                if (!expectedChar(']')) {
                    throw new ParseException("\"]\" expected", offset);
                }
            }
            object = list.toArray();
//            object = list.toArray(Array.newInstance(list.get(0).getClass(), 0));
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
                    throw new ParseException("Unknown literal: " + literal, offset);
            }
        } else if (charIn(NUMBERS, lastChar())) {
            String number = nextChars(NUMBERS);
            try {
                object = toNumber(number);
            } catch (NumberFormatException e) {
                throw new ParseException("Number format: " + number, offset);
            }
        } else {
            throw new ParseException("Unexpected char: " + errorChar(), offset); // !
        }
        skipWhitespaces(); // trailing
        return object;
    }

    static Number toNumber(String number)
            throws NumberFormatException {
//          return new BigDecimal(number);
        if (number.indexOf('.') >= 0 || number.indexOf('E') >= 0 || number.indexOf('e') >= 0) {
            return Double.parseDouble(number);
        }
        return Long.parseLong(number);
    }

    private static final char[] ESCAPED_CHARS = {'"', '/', '\\', 'b', 'f', 'n', 'r', 't'};
    private static final char[] CHARS_UNESCAPED = {0x22, 0x2F, 0x5C, 0x8, 0xC, 0xA, 0xD, 0x9};

    static String unescapeString(String s) throws ParseException {
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
}
