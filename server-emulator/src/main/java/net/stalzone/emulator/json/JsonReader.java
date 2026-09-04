package net.stalzone.emulator.json;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A minimal, dependency-free JSON parser. Exists because this project's sandbox can't fetch
 * a real JSON library (no Maven Central access -- see server-emulator/README.md's "Why no
 * Netty" section for the same constraint applied to zstd/Netty) and the JDK ships no JSON
 * parser of its own. Just enough standard JSON to read data/weapons/weapons.json and similar
 * plain-data files this project produces: objects, arrays, strings (with standard escapes),
 * numbers (parsed as Double -- JSON doesn't distinguish int/float, callers narrow as needed),
 * booleans, and null. Not a general-purpose/streaming parser -- reads the whole document into
 * memory as nested Map&lt;String,Object&gt; / List&lt;Object&gt; / String / Double / Boolean / null,
 * which is exactly what a config/data file this size needs and no more.
 */
public final class JsonReader {

    private final String text;
    private int pos;

    private JsonReader(String text) {
        this.text = text;
        this.pos = 0;
    }

    public static Object parse(String text) {
        JsonReader r = new JsonReader(text);
        r.skipWhitespace();
        Object value = r.parseValue();
        r.skipWhitespace();
        if (r.pos != r.text.length()) {
            throw new JsonParseException("Unexpected trailing content at offset " + r.pos);
        }
        return value;
    }

    private Object parseValue() {
        if (pos >= text.length()) {
            throw new JsonParseException("Unexpected end of input");
        }
        char c = text.charAt(pos);
        switch (c) {
            case '{': return parseObject();
            case '[': return parseArray();
            case '"': return parseString();
            case 't': expect("true"); return Boolean.TRUE;
            case 'f': expect("false"); return Boolean.FALSE;
            case 'n': expect("null"); return null;
            default:
                if (c == '-' || (c >= '0' && c <= '9')) {
                    return parseNumber();
                }
                throw new JsonParseException("Unexpected character '" + c + "' at offset " + pos);
        }
    }

    private Map<String, Object> parseObject() {
        Map<String, Object> map = new LinkedHashMap<>();
        pos++; // consume '{'
        skipWhitespace();
        if (peek() == '}') {
            pos++;
            return map;
        }
        while (true) {
            skipWhitespace();
            if (peek() != '"') {
                throw new JsonParseException("Expected string key at offset " + pos);
            }
            String key = parseString();
            skipWhitespace();
            if (peek() != ':') {
                throw new JsonParseException("Expected ':' at offset " + pos);
            }
            pos++; // consume ':'
            skipWhitespace();
            Object value = parseValue();
            map.put(key, value);
            skipWhitespace();
            char c = peek();
            if (c == ',') {
                pos++;
                continue;
            }
            if (c == '}') {
                pos++;
                break;
            }
            throw new JsonParseException("Expected ',' or '}' at offset " + pos);
        }
        return map;
    }

    private List<Object> parseArray() {
        List<Object> list = new ArrayList<>();
        pos++; // consume '['
        skipWhitespace();
        if (peek() == ']') {
            pos++;
            return list;
        }
        while (true) {
            skipWhitespace();
            list.add(parseValue());
            skipWhitespace();
            char c = peek();
            if (c == ',') {
                pos++;
                continue;
            }
            if (c == ']') {
                pos++;
                break;
            }
            throw new JsonParseException("Expected ',' or ']' at offset " + pos);
        }
        return list;
    }

    private String parseString() {
        StringBuilder sb = new StringBuilder();
        pos++; // consume opening '"'
        while (true) {
            if (pos >= text.length()) {
                throw new JsonParseException("Unterminated string");
            }
            char c = text.charAt(pos++);
            if (c == '"') {
                break;
            }
            if (c == '\\') {
                if (pos >= text.length()) {
                    throw new JsonParseException("Unterminated escape sequence");
                }
                char esc = text.charAt(pos++);
                switch (esc) {
                    case '"': sb.append('"'); break;
                    case '\\': sb.append('\\'); break;
                    case '/': sb.append('/'); break;
                    case 'b': sb.append('\b'); break;
                    case 'f': sb.append('\f'); break;
                    case 'n': sb.append('\n'); break;
                    case 'r': sb.append('\r'); break;
                    case 't': sb.append('\t'); break;
                    case 'u':
                        if (pos + 4 > text.length()) {
                            throw new JsonParseException("Truncated \\u escape");
                        }
                        String hex = text.substring(pos, pos + 4);
                        sb.append((char) Integer.parseInt(hex, 16));
                        pos += 4;
                        break;
                    default:
                        throw new JsonParseException("Unknown escape '\\" + esc + "' at offset " + (pos - 1));
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private Double parseNumber() {
        int start = pos;
        if (peek() == '-') {
            pos++;
        }
        while (pos < text.length() && Character.isDigit(text.charAt(pos))) {
            pos++;
        }
        if (pos < text.length() && text.charAt(pos) == '.') {
            pos++;
            while (pos < text.length() && Character.isDigit(text.charAt(pos))) {
                pos++;
            }
        }
        if (pos < text.length() && (text.charAt(pos) == 'e' || text.charAt(pos) == 'E')) {
            pos++;
            if (pos < text.length() && (text.charAt(pos) == '+' || text.charAt(pos) == '-')) {
                pos++;
            }
            while (pos < text.length() && Character.isDigit(text.charAt(pos))) {
                pos++;
            }
        }
        return Double.parseDouble(text.substring(start, pos));
    }

    private void expect(String literal) {
        if (pos + literal.length() > text.length() || !text.regionMatches(pos, literal, 0, literal.length())) {
            throw new JsonParseException("Expected '" + literal + "' at offset " + pos);
        }
        pos += literal.length();
    }

    private char peek() {
        if (pos >= text.length()) {
            throw new JsonParseException("Unexpected end of input");
        }
        return text.charAt(pos);
    }

    private void skipWhitespace() {
        while (pos < text.length()) {
            char c = text.charAt(pos);
            if (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
                pos++;
            } else {
                break;
            }
        }
    }

    public static final class JsonParseException extends RuntimeException {
        public JsonParseException(String message) {
            super(message);
        }
    }
}
