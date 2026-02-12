package com.extractor.languages;

import com.extractor.core.BaseParser;
import com.extractor.core.ParserRule;
import com.extractor.core.SourceScanner;
import com.extractor.model.CodeNode;
import com.extractor.model.NodeType;

import java.util.List;
import java.util.Optional;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A robust Java parser that extracts classes, methods, annotations, and comments.
 * It uses a stack-based approach to handle nested scopes and regex rules for identification.
 */
public class JavaParser extends BaseParser {

    private final List<ParserRule> blockRules;
    private final List<ParserRule> statementRules;

    private static final Pattern METHOD_NAME_PATTERN = Pattern.compile("\\b([a-zA-Z_][a-zA-Z0-9_]*)\\s*\\(");
    private static final Pattern ANNOTATION_PATTERN = Pattern.compile("@([a-zA-Z_][a-zA-Z0-9_.]*)(\\s*\\([^\\)]*\\))?");
    private static final Pattern ANNOTATION_DEF_PATTERN = Pattern.compile("@interface\\s+([a-zA-Z0-9_$.]+)");

    public JavaParser() {
        super("java");
        
        // Rules for identifying the type of a block starting with '{'
        this.blockRules = List.of(
            (ctx, pos) -> {
                String clean = stripComments(ctx).trim();
                String name = extractIdentifierAfterKeyword(clean, "class");
                return name != null ? Optional.of(new CodeNode(NodeType.CLASS, name, pos)) : Optional.empty();
            },
            (ctx, pos) -> {
                String clean = stripComments(ctx).trim();
                String name = extractIdentifierAfterKeyword(clean, "interface");
                return name != null ? Optional.of(new CodeNode(NodeType.INTERFACE, name, pos)) : Optional.empty();
            },
            (ctx, pos) -> {
                String clean = stripComments(ctx).trim();
                String name = extractIdentifierAfterKeyword(clean, "enum");
                return name != null ? Optional.of(new CodeNode(NodeType.ENUM, name, pos)) : Optional.empty();
            },
            (ctx, pos) -> {
                String clean = stripComments(ctx).trim();
                if (clean.contains("@interface")) {
                    Matcher m = ANNOTATION_DEF_PATTERN.matcher(clean);
                    if (m.find()) {
                        return Optional.of(new CodeNode(NodeType.ANNOTATION, m.group(1), pos));
                    }
                }
                if (isMethodDeclaration(clean)) {
                    String name = extractMethodName(clean);
                    return Optional.of(new CodeNode(NodeType.METHOD, name, pos));
                }
                return Optional.empty();
            }
        );

        // Rules for identifying statements ending with ';'
        this.statementRules = List.of(
            (ctx, pos) -> {
                String clean = stripComments(ctx).trim();
                if (clean.startsWith("package ")) {
                    return Optional.of(new CodeNode(NodeType.NAMESPACE, extractIdentifierAfterKeyword(clean, "package"), pos));
                }
                return Optional.empty();
            },
            (ctx, pos) -> {
                String clean = stripComments(ctx).trim();
                if (clean.startsWith("import ")) {
                    return Optional.of(new CodeNode(NodeType.IMPORT, extractIdentifierAfterKeyword(clean, "import"), pos));
                }
                return Optional.empty();
            }
        );
    }

    @Override
    protected boolean isMethodDeclaration(String text) {
        String clean = stripComments(text).trim();
        if (clean.isEmpty()) return false;

        // Exclude class-level keywords
        if (clean.contains("class ") || clean.contains("interface ") || clean.contains("enum ") || clean.contains("@interface")) {
            return false;
        }
        
        // Avoid control structures, initializers, and anonymous class creation
        if (clean.matches("(?s)^\\s*(?:if|for|while|switch|catch|synchronized|static|new|return)\\b.*")) {
            return false;
        }
        
        // Avoid lambdas which are usually blocks within methods
        if (clean.contains("->")) {
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
        while (matcher.find()) {
            String name = matcher.group(1);
            if (!isControlKeyword(name)) {
                return name;
            }
        }
        return "unknown";
    }

    @Override
    protected boolean isControlKeyword(String name) {
        return name.equals("if") || name.equals("for") || name.equals("while") || 
               name.equals("switch") || name.equals("catch") || name.equals("synchronized") || 
               name.equals("new") || name.equals("static") || name.equals("return");
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
            } else if (c == '/' && scanner.peekNext() == '*') {
                scopeStack.peek().addChild(extractBlockComment(scanner, "*/"));
            } 
            // Handle strings to avoid misinterpreting content
            else if (c == '"' || c == '\'') {
                skipString(scanner);
            } 
            // Handle annotations
            else if (c == '@') {
                String remaining = source.substring(scanner.getPos());
                Matcher matcher = ANNOTATION_PATTERN.matcher(remaining);
                if (matcher.find() && matcher.start() == 0) {
                    String match = matcher.group();
                    CodeNode node = new CodeNode(NodeType.ANNOTATION, matcher.group(1), scanner.getPos());
                    node.setEndOffset(scanner.getPos() + match.length());
                    node.setContent(match);
                    scopeStack.peek().addChild(node);
                    scanner.advance(match.length());
                } else {
                    scanner.advance();
                }
            } 
            // Handle scope start
            else if (c == '{') {
                String lookback = source.substring(lastPos, scanner.getPos());
                CodeNode node = applyRules(blockRules, lookback, lastPos)
                        .orElse(new CodeNode(NodeType.BLOCK, "anonymous", lastPos));
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
                applyRules(statementRules, statement, lastPos).ifPresent(node -> {
                    node.setEndOffset(scanner.getPos());
                    node.setContent(statement.trim());
                    scopeStack.peek().addChild(node);
                });
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
}
