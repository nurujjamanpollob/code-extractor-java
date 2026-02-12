package com.extractor.languages;

import com.extractor.core.BaseParser;
import com.extractor.core.SourceScanner;
import com.extractor.model.CodeNode;
import com.extractor.model.NodeType;

import java.util.Stack;

public class SwiftParser extends BaseParser {
    public SwiftParser() {
        super("swift");
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
                    CodeNode newNode = identifySwiftNode(lookback, lastPos);
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
                } else if (c == '"') {
                    skipString(scanner);
                } else if (c == '/' && scanner.peekNext() == '/') {
                    scopeStack.peek().addChild(extractLineComment(scanner));
                    lastPos = scanner.getPos();
                } else if (c == '/' && scanner.peekNext() == '*') {
                    scopeStack.peek().addChild(extractBlockComment(scanner, "*/"));
                    lastPos = scanner.getPos();
                } else {
                    scanner.advance();
                }
            }
        } catch (Exception e) {
            // Ignore parsing errors for robustness
        }

        finalizeScope(scopeStack, source.length(), source);
        root.setEndOffset(source.length());
        root.setContent(source);
        return root;
    }

    private CodeNode identifySwiftNode(String lookback, int pos) {
        if (lookback.contains("class ")) {
            String name = extractIdentifierAfterKeyword(lookback, "class");
            if (name != null) return new CodeNode(NodeType.CLASS, name, pos);
        }
        if (lookback.contains("struct ")) {
            String name = extractIdentifierAfterKeyword(lookback, "struct");
            if (name != null) return new CodeNode(NodeType.STRUCT, name, pos);
        }
        if (lookback.contains("enum ")) {
            String name = extractIdentifierAfterKeyword(lookback, "enum");
            if (name != null) return new CodeNode(NodeType.ENUM, name, pos);
        }
        if (lookback.contains("protocol ")) {
            String name = extractIdentifierAfterKeyword(lookback, "protocol");
            if (name != null) return new CodeNode(NodeType.INTERFACE, name, pos);
        }
        if (lookback.contains("extension ")) {
            String name = extractIdentifierAfterKeyword(lookback, "extension");
            if (name != null) return new CodeNode(NodeType.CLASS, name, pos);
        }
        if (lookback.contains("func ")) {
            String name = extractIdentifierAfterKeyword(lookback, "func");
            if (name == null || name.isEmpty() || name.equals("unknown")) {
                name = extractMethodName(lookback);
            }
            return new CodeNode(NodeType.METHOD, name, pos);
        }
        if (lookback.contains("init")) {
            // Check for 'init' as a word, potentially followed by '(', '?', or '!'
            if (lookback.matches("(?s).*\\binit[!?]?\\s*\\(.*")) {
                return new CodeNode(NodeType.CONSTRUCTOR, "init", pos);
            }
        }
        
        return new CodeNode(NodeType.BLOCK, "anonymous", pos);
    }
}
