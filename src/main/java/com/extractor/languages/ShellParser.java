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

public class ShellParser extends BaseParser {
    private final List<ParserRule> rules = new ArrayList<>();

    public ShellParser() {
        super("bash");
        setupRules();
    }

    private void setupRules() {
        rules.add((text, pos) -> {
            if (text.contains("()")) {
                String name = text.substring(0, text.indexOf("()")).trim();
                int lastSpace = name.lastIndexOf(' ');
                if (lastSpace != -1) name = name.substring(lastSpace + 1);
                return Optional.of(new CodeNode(NodeType.FUNCTION, name, pos));
            }
            if (text.startsWith("function ")) {
                return Optional.of(new CodeNode(NodeType.FUNCTION, extractIdentifierAfterKeyword(text, "function"), pos));
            }
            return Optional.empty();
        });
        rules.add((text, pos) -> {
            if (text.contains("=") && !text.startsWith("if ") && !text.startsWith("while ")) {
                String name = text.split("=")[0].trim();
                if (name.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
                    return Optional.of(new CodeNode(NodeType.VARIABLE, name, pos));
                }
            }
            return Optional.empty();
        });
    }

    @Override
    public CodeNode parse(String source) {
        if (source == null) return new CodeNode(NodeType.ROOT, "root", 0);
        CodeNode root = new CodeNode(NodeType.ROOT, "root", 0);
        SourceScanner scanner = new SourceScanner(source);
        Stack<CodeNode> scopeStack = new Stack<>();
        scopeStack.push(root);

        int lastPos = 0;
        try {
            while (!scanner.isAtEnd()) {
                char c = scanner.peek();
                if (c == '{') {
                    String lookback = scanner.substring(lastPos, scanner.getPos()).trim();
                    CodeNode newNode = applyRules(rules, lookback, lastPos).orElse(new CodeNode(NodeType.BLOCK, "anonymous", lastPos));
                    scopeStack.peek().addChild(newNode);
                    scopeStack.push(newNode);
                    scanner.advance();
                    lastPos = scanner.getPos();
                } else if (c == '}') {
                    if (scopeStack.size() > 1) {
                        CodeNode closed = scopeStack.pop();
                        closed.setEndOffset(scanner.getPos() + 1);
                        closed.setContent(source.substring(closed.getStartOffset(), closed.getEndOffset()));
                    }
                    scanner.advance();
                    lastPos = scanner.getPos();
                } else if (c == '#') {
                    scopeStack.peek().addChild(extractLineComment(scanner, "#"));
                    lastPos = scanner.getPos();
                } else if (c == '"' || c == '\'') {
                    skipString(scanner);
                } else if (c == '\n' || c == ';') {
                    String line = scanner.substring(lastPos, scanner.getPos()).trim();
                    if (!line.isEmpty()) {
                        applyRules(rules, line, lastPos).ifPresent(node -> {
                            node.setEndOffset(scanner.getPos());
                            node.setContent(source.substring(node.getStartOffset(), node.getEndOffset()));
                            scopeStack.peek().addChild(node);
                        });
                    }
                    scanner.advance();
                    lastPos = scanner.getPos();
                } else {
                    scanner.advance();
                }
            }
        } catch (Exception e) {}

        finalizeScope(scopeStack, source.length(), source);
        root.setEndOffset(source.length());
        root.setContent(source);
        return root;
    }
}
