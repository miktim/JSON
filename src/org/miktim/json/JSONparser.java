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
import static org.miktim.json.JSON.toNumber;
import static org.miktim.json.JSON.unescapeString;

class JSONparser {

    String charsetName;
    InputStream stream;

    JSONparser(InputStream inStream, String charsetName) throws UnsupportedEncodingException {
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

    Object parseObject() throws IOException, ParseException {
//            skipWhitespaces(); // leading
        Object object = null;
        if (expectedChar('{')) { // JSON object
            object = new Json();
            if (!expectedChar('}')) { // empty object
                do {
                    Object memberName = parseObject();
                    if ((memberName instanceof String) && expectedChar(':')) {
                        ((Json) object).set((String) memberName, parseObject());
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
            List<Object> list = new ArrayList<Object>(); //
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
                object = toNumber(number);
            } catch (NumberFormatException e) {
                throw newParseException("Number format:",
                        number, offset);
            }
        } else {
            throw newParseException("Unexpected char:",
                    Character.toString(lastChar()), offset + 1); // !
        }
        skipWhitespaces(); // trailing
        return object;
    }
}
