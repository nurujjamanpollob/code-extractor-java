package com.extractor.malformed;

public class Malformed {

    public void missingBrace() {
        if (true) {
            System.out.println("No closing brace for if");
    }

    public void unclosedString() {
        String s = "This string never ends;
    }

    public void unclosedComment() {
        /* This comment is never closed
    }

    // Unclosed class brace follows
