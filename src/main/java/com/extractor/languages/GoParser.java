package com.extractor.languages;

import com.extractor.core.BaseParser;
import com.extractor.core.ParserRule;
import com.extractor.core.SourceScanner;
import com.extractor.model.CodeNode;
import com.extractor.model.NodeType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GoParser extends BaseParser {

    public GoParser() {
        super("go");
    }

    @Override
    public CodeNode parse(String source) {
        CodeNode root = new CodeNode(NodeType.ROOT, "root", 0);
        if (source == null || source.isEmpty()) return root;

        SourceScanner scanner = new SourceScanner(source);
        Stack<CodeNode> scopeStack = new Stack<>();
        scopeStack.push(root);

        List<ParserRule> rules = createRules();

        int lastPos = 0;
        while (!scanner.isAtEnd()) {
            char c = scanner.peek();

            if (c == '{') {
                String lookback = source.substring(lastPos, scanner.getPos()).trim();
                CodeNode newNode = applyRules(rules, lookback, lastPos).orElse(new CodeNode(NodeType.BLOCK, "anonymous", lastPos));
                scopeStack.peek().addChild(newNode);
                scopeStack.push(newNode);
                scanner.advance();
                lastPos = scanner.getPos();
            } else if (c == '}') {
                if (scopeStack.size() > 1) {
                    CodeNode closed = scopeStack.pop();
                    closed.setEndOffset(scanner.getPos() + 1);
                    closed.setContent(source.substring(closed.getStartOffset(), Math.min(closed.getEndOffset(), source.length())));
                }
                scanner.advance();
                lastPos = scanner.getPos();
            } else if (c == '"' || c == '`') {
                skipString(scanner);
            } else if (c == '/' && scanner.peekNext() == '/') {
                scopeStack.peek().addChild(extractLineComment(scanner));
                lastPos = scanner.getPos();
            } else if (c == '/' && scanner.peekNext() == '*') {
                scopeStack.peek().addChild(extractBlockComment(scanner, "*/"));
                lastPos = scanner.getPos();
            } else if (c == '\n') {
                String line = source.substring(lastPos, scanner.getPos()).trim();
                if (!line.isEmpty()) {
                    applyRules(rules, line, lastPos).ifPresent(node -> {
                        node.setEndOffset(scanner.getPos());
                        node.setContent(line);
                        scopeStack.peek().addChild(node);
                    });
                }
                scanner.advance();
                lastPos = scanner.getPos();
            } else {
                scanner.advance();
            }
        }

        finalizeScope(scopeStack, source.length(), source);
        root.setEndOffset(source.length());
        root.setContent(source);
        return root;
    }

    private List<ParserRule> createRules() {
        List<ParserRule> rules = new ArrayList<>();

        // Package
        rules.add((text, pos) -> {
            if (text.contains("package ")) {
                String name = extractIdentifierAfterKeyword(text, "package");
                if (name != null) {
                    return Optional.of(new CodeNode(NodeType.MODULE, name, pos));
                }
            }
            return Optional.empty();
        });

        // Imports
        rules.add((text, pos) -> {
            if (text.contains("import ")) {
                String name = extractIdentifierAfterKeyword(text, "import");
                if (name != null) {
                    return Optional.of(new CodeNode(NodeType.IMPORT, name.replace("\"", ""), pos));
                }
            }
            return Optional.empty();
        });

        // Functions and Methods
        rules.add((text, pos) -> {
            if (text.contains("func ")) {
                NodeType type = text.matches(".*func\\s+\\([^)]+\\).*") ? NodeType.METHOD : NodeType.FUNCTION;
                String name = extractGoFunctionName(text);
                if (name != null) {
                    return Optional.of(new CodeNode(type, name, pos));
                }
            }
            return Optional.empty();
        });

        // Structs
        rules.add((text, pos) -> {
            if (text.contains("struct")) {
                String name = extractGoTypeName(text, "struct");
                if (name != null) {
                    return Optional.of(new CodeNode(NodeType.STRUCT, name, pos));
                }
            }
            return Optional.empty();
        });

        // Interfaces
        rules.add((text, pos) -> {
            if (text.contains("interface")) {
                String name = extractGoTypeName(text, "interface");
                if (name != null) {
                    return Optional.of(new CodeNode(NodeType.INTERFACE, name, pos));
                }
            }
            return Optional.empty();
        });

        return rules;
    }

    private String extractGoFunctionName(String text) {
        // Handle both func Name() and func (r Receiver) Name()
        Pattern pattern = Pattern.compile("func\\s+(?:\\([^)]+\\)\\s*)?([a-zA-Z0-9_]+)");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private String extractGoTypeName(String text, String typeKeyword) {
        // Matches "type Name struct" or "Name struct" (inside a type block)
        Pattern pattern = Pattern.compile("(?:type\\s+)?([a-zA-Z0-9_]+)\\s+" + typeKeyword);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
