package com.extractor.core;

public class SourceScanner {
    private final String source;
    private int pos = 0;
    private final int length;

    public SourceScanner(String source) {
        this.source = source != null ? source : "";
        this.length = this.source.length();
    }

    public boolean isAtEnd() {
        return pos >= length;
    }

    public char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(pos);
    }

    public char peekNext() {
        if (pos + 1 >= length) return '\0';
        return source.charAt(pos + 1);
    }

    public char advance() {
        if (isAtEnd()) return '\0';
        return source.charAt(pos++);
    }

    public void advance(int n) {
        pos = Math.min(pos + n, length);
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        if (pos >= 0 && pos <= length) {
            this.pos = pos;
        }
    }

    public String substring(int start, int end) {
        return source.substring(start, Math.min(end, length));
    }

    public String peek(int n) {
        int end = Math.min(pos + n, length);
        return source.substring(pos, end);
    }

    public void skipWhitespace() {
        while (!isAtEnd() && Character.isWhitespace(peek())) {
            advance();
        }
    }

    public boolean match(String expected) {
        if (pos + expected.length() > length) return false;
        if (source.substring(pos, pos + expected.length()).equals(expected)) {
            pos += expected.length();
            return true;
        }
        return false;
    }
}
