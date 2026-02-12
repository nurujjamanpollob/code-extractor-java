package com.extractor.languages;

import com.extractor.core.BaseParser;
import com.extractor.core.SourceScanner;
import com.extractor.model.CodeNode;
import com.extractor.model.NodeType;

import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A parser for Dart code that extracts classes, enums, and functions.
 */
public class DartParser extends BaseParser {

    private static final Pattern METHOD_NAME_PATTERN = Pattern.compile("\\b([a-zA-Z_][a-zA-Z0-9_]*)\\s*\\(");

    public DartParser() {
        super("dart");
    }

    @Override
    public CodeNode parse(String source) {
        CodeNode root = new CodeNode(NodeType.ROOT, "root", 0);
        if (source == null || source.isEmpty()) return root;

        SourceScanner scanner = new SourceScanner(source);
        Stack<CodeNode> scopeStack = new Stack<>();
        scopeStack.push(root);

        int lastPos = 0;

        while (!scanner.isAtEnd()) {
            char c = scanner.peek();

            // Handle comments
            if (c == '/' && scanner.peekNext() == '/') {
                scopeStack.peek().addChild(extractLineComment(scanner));
                lastPos = scanner.getPos();
            } else if (c == '/' && scanner.peekNext() == '*') {
                scopeStack.peek().addChild(extractBlockComment(scanner, "*/"));
                lastPos = scanner.getPos();
            } 
            // Handle strings to avoid misinterpreting content
            else if (c == '"' || c == '\'') {
                skipString(scanner);
            } 
            // Handle scope start
            else if (c == '{') {
                String lookback = source.substring(lastPos, scanner.getPos());
                CodeNode node = identifyDartNode(lookback, lastPos);
                scopeStack.peek().addChild(node);
                scopeStack.push(node);
                scanner.advance();
                lastPos = scanner.getPos();
            } 
            // Handle scope end
            else if (c == '}') {
                if (scopeStack.size() > 1) {
                    CodeNode closed = scopeStack.pop();
                    closed.setEndOffset(scanner.getPos() + 1);
                    closed.setContent(source.substring(closed.getStartOffset(), Math.min(closed.getEndOffset(), source.length())));
                }
                scanner.advance();
                lastPos = scanner.getPos();
            } 
            // Handle statements
            else if (c == ';') {
                String statement = source.substring(lastPos, scanner.getPos());
                CodeNode node = identifyDartStatement(statement, lastPos);
                if (node != null) {
                    node.setEndOffset(scanner.getPos());
                    node.setContent(statement.trim());
                    scopeStack.peek().addChild(node);
                }
                scanner.advance();
                lastPos = scanner.getPos();
            } 
            // Advance scanner
            else {
                scanner.advance();
            }
        }

        finalizeScope(scopeStack, source.length(), source);
        root.setEndOffset(source.length());
        root.setContent(source);
        return root;
    }

    private CodeNode identifyDartNode(String lookback, int pos) {
        String clean = stripComments(lookback).trim();
        
        if (clean.contains("class ")) {
            return new CodeNode(NodeType.CLASS, extractIdentifierAfterKeyword(clean, "class"), pos);
        } else if (clean.contains("enum ")) {
            return new CodeNode(NodeType.ENUM, extractIdentifierAfterKeyword(clean, "enum"), pos);
        } else if (clean.contains("extension ")) {
            return new CodeNode(NodeType.CLASS, extractIdentifierAfterKeyword(clean, "extension"), pos);
        } else if (clean.contains("mixin ")) {
            return new CodeNode(NodeType.CLASS, extractIdentifierAfterKeyword(clean, "mixin"), pos);
        } else if (isMethodDeclaration(clean)) {
            return new CodeNode(NodeType.FUNCTION, extractMethodName(clean), pos);
        }
        
        return new CodeNode(NodeType.BLOCK, "block", pos);
    }

    private CodeNode identifyDartStatement(String statement, int pos) {
        String clean = stripComments(statement).trim();
        if (clean.startsWith("import ")) {
            return new CodeNode(NodeType.MODULE, extractIdentifierAfterKeyword(clean, "import"), pos);
        } else if (clean.startsWith("library ")) {
            return new CodeNode(NodeType.MODULE, extractIdentifierAfterKeyword(clean, "library"), pos);
        } else if (clean.startsWith("part ")) {
            return new CodeNode(NodeType.MODULE, extractIdentifierAfterKeyword(clean, "part"), pos);
        } else if (clean.startsWith("export ")) {
            return new CodeNode(NodeType.MODULE, extractIdentifierAfterKeyword(clean, "export"), pos);
        }
        return null;
    }

    @Override
    protected boolean isMethodDeclaration(String text) {
        String clean = stripComments(text).trim();
        if (clean.isEmpty()) return false;

        // Exclude class-level keywords that might be in the lookback
        if (clean.contains("class ") || clean.contains("enum ") || clean.contains("extension ") || clean.contains("mixin ")) {
            return false;
        }

        // Avoid control structures
        if (clean.matches("(?s)^\\s*(?:if|for|while|switch|catch|new|return|try|finally)\\b.*")) {
            return false;
        }

        // Must have a method-like name followed by '('
        Matcher matcher = METHOD_NAME_PATTERN.matcher(clean);
        if (matcher.find()) {
            String name = matcher.group(1);
            return !isControlKeyword(name);
        }
        
        return false;
    }

    @Override
    protected String extractMethodName(String text) {
        Matcher matcher = METHOD_NAME_PATTERN.matcher(text);
        String lastMatch = "unknown";
        while (matcher.find()) {
            String name = matcher.group(1);
            if (!isControlKeyword(name)) {
                lastMatch = name;
            }
        }
        return lastMatch;
    }

    @Override
    protected boolean isControlKeyword(String name) {
        return name.equals("if") || name.equals("for") || name.equals("while") || 
               name.equals("switch") || name.equals("catch") || name.equals("new") || 
               name.equals("return") || name.equals("try") || name.equals("finally") ||
               name.equals("sync") || name.equals("async");
    }
}
