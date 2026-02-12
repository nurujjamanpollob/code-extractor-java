package com.extractor.languages;

import com.extractor.core.BaseParser;
import com.extractor.core.SourceScanner;
import com.extractor.model.CodeNode;
import com.extractor.model.NodeType;

import java.util.Stack;

public class HTMLParser extends BaseParser {

    public HTMLParser() {
        super("html");
    }

    @Override
    public CodeNode parse(String source) {
        CodeNode root = new CodeNode(NodeType.ROOT, "root", 0);
        if (source == null || source.isEmpty()) return root;

        SourceScanner scanner = new SourceScanner(source);
        Stack<CodeNode> scopeStack = new Stack<>();
        scopeStack.push(root);

        while (!scanner.isAtEnd()) {
            char c = scanner.peek();

            if (c == '<') {
                if (scanner.peekNext() == '!') {
                    if (scanner.match("<!--")) {
                        scopeStack.peek().addChild(extractBlockComment(scanner, "-->"));
                    } else {
                        scanner.advance();
                    }
                } else if (scanner.peekNext() == '/') {
                    // Close tag
                    scanner.advance();
                    scanner.advance();
                    String tagName = consumeIdentifier(scanner);
                    while (!scanner.isAtEnd() && scanner.peek() != '>') {
                        scanner.advance();
                    }
                    if (!scanner.isAtEnd()) {
                        scanner.advance(); // >
                    }
                    
                    if (scopeStack.size() > 1 && scopeStack.peek().getName().equals(tagName)) {
                        CodeNode closed = scopeStack.pop();
                        closed.setEndOffset(scanner.getPos());
                        closed.setContent(source.substring(closed.getStartOffset(), closed.getEndOffset()));
                    }
                } else {
                    // Open tag
                    int start = scanner.getPos();
                    scanner.advance();
                    String tagName = consumeIdentifier(scanner);
                    CodeNode node = new CodeNode(NodeType.TAG, tagName, start);
                    
                    while (!scanner.isAtEnd() && scanner.peek() != '>') {
                        if (scanner.peek() == '"' || scanner.peek() == '\'') {
                            skipString(scanner);
                        } else {
                            scanner.advance();
                        }
                    }
                    
                    boolean selfClosing = false;
                    if (scanner.getPos() > 0 && source.charAt(scanner.getPos() - 1) == '/') {
                        selfClosing = true;
                    }

                    if (!scanner.isAtEnd()) {
                        scanner.advance(); // >
                    }

                    if (selfClosing || isVoidElement(tagName)) {
                        node.setEndOffset(scanner.getPos());
                        node.setContent(source.substring(node.getStartOffset(), node.getEndOffset()));
                        scopeStack.peek().addChild(node);
                    } else {
                        scopeStack.peek().addChild(node);
                        scopeStack.push(node);
                    }
                }
            } else {
                scanner.advance();
            }
        }

        finalizeScope(scopeStack, source.length(), source);
        root.setEndOffset(source.length());
        root.setContent(source);
        return root;
    }

    private String consumeIdentifier(SourceScanner scanner) {
        StringBuilder sb = new StringBuilder();
        while (!scanner.isAtEnd() && (Character.isLetterOrDigit(scanner.peek()) || scanner.peek() == '-' || scanner.peek() == '_' || scanner.peek() == ':')) {
            sb.append(scanner.advance());
        }
        return sb.toString();
    }

    private boolean isVoidElement(String tag) {
        return "area".equalsIgnoreCase(tag) || "base".equalsIgnoreCase(tag) || "br".equalsIgnoreCase(tag) || "col".equalsIgnoreCase(tag) ||
               "embed".equalsIgnoreCase(tag) || "hr".equalsIgnoreCase(tag) || "img".equalsIgnoreCase(tag) || "input".equalsIgnoreCase(tag) ||
               "link".equalsIgnoreCase(tag) || "meta".equalsIgnoreCase(tag) || "param".equalsIgnoreCase(tag) || "source".equalsIgnoreCase(tag) ||
               "track".equalsIgnoreCase(tag) || "wbr".equalsIgnoreCase(tag);
    }
}
