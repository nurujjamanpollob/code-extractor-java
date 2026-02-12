package com.extractor.core;

import com.extractor.model.CodeNode;
import com.extractor.model.NodeType;

import java.util.List;
import java.util.Optional;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Base class for all language parsers, providing common utilities for node extraction.
 */
public abstract class BaseParser implements CodeParser {
    protected final String language;

    protected BaseParser(String language) {
        this.language = language;
    }

    @Override
    public boolean supports(String language) {
        return this.language.equalsIgnoreCase(language);
    }

    /**
     * Applies a list of rules to the given context and returns the first matching node.
     */
    protected Optional<CodeNode> applyRules(List<ParserRule> rules, String context, int pos) {
        for (ParserRule rule : rules) {
            Optional<CodeNode> result = rule.apply(context, pos);
            if (result.isPresent()) {
                return result;
            }
        }
        return Optional.empty();
    }

    /**
     * Extracts the identifier immediately following a keyword (e.g., class Name, function name).
     */
    protected String extractIdentifierAfterKeyword(String text, String keyword) {
        String boundary = keyword.startsWith("@") ? "(?<!\\w)" : "\\b";
        Pattern pattern = Pattern.compile(boundary + Pattern.quote(keyword) + "\\s+([a-zA-Z0-9_$.]+)");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * Removes single-line and multi-line comments from the text.
     */
    protected String stripComments(String text) {
        if (text == null) return "";
        // Updated to handle multi-line comments correctly using DOTALL flag (?s) for the block comment part
        return text.replaceAll("//.*|(?s:/\\*.*?\\*/)", "");
    }

    /**
     * Skips a string literal in the scanner.
     */
    protected void skipString(SourceScanner scanner) {
        char quote = scanner.peek();
        scanner.advance();
        while (!scanner.isAtEnd()) {
            if (scanner.peek() == '\\') {
                scanner.advance(2);
            } else if (scanner.peek() == quote) {
                scanner.advance();
                break;
            } else {
                scanner.advance();
            }
        }
    }

    protected CodeNode extractLineComment(SourceScanner scanner) {
        return extractLineComment(scanner, "//");
    }

    protected CodeNode extractLineComment(SourceScanner scanner, String delimiter) {
        int start = scanner.getPos();
        scanner.advance(delimiter.length());
        while (!scanner.isAtEnd() && scanner.peek() != '\n') {
            scanner.advance();
        }
        String content = scanner.substring(start, scanner.getPos());
        CodeNode node = new CodeNode(NodeType.COMMENT, "line_comment", start);
        node.setEndOffset(scanner.getPos());
        node.setContent(content);
        return node;
    }

    protected CodeNode extractBlockComment(SourceScanner scanner, String endDelimiter) {
        int start = scanner.getPos();
        scanner.advance(2); // Skip /* or similar
        while (!scanner.isAtEnd() && !scanner.peek(endDelimiter.length()).equals(endDelimiter)) {
            scanner.advance();
        }
        if (!scanner.isAtEnd()) {
            scanner.advance(endDelimiter.length());
        }
        String content = scanner.substring(start, scanner.getPos());
        CodeNode node = new CodeNode(NodeType.COMMENT, "block_comment", start);
        node.setEndOffset(scanner.getPos());
        node.setContent(content);
        return node;
    }

    protected void finalizeScope(Stack<CodeNode> scopeStack, int endPos, String source) {
        while (scopeStack.size() > 1) {
            CodeNode node = scopeStack.pop();
            node.setEndOffset(endPos);
            node.setContent(source.substring(node.getStartOffset(), endPos));
        }
    }

    protected boolean isMethodDeclaration(String text) {
        return false;
    }

    protected String extractMethodName(String text) {
        return null;
    }

    protected boolean isControlKeyword(String name) {
        return false;
    }
}
