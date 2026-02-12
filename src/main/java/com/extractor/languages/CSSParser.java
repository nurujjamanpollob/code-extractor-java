package com.extractor.languages;

import com.extractor.core.BaseParser;
import com.extractor.core.SourceScanner;
import com.extractor.model.CodeNode;
import com.extractor.model.NodeType;

import java.util.Stack;

public class CSSParser extends BaseParser {

    public CSSParser() {
        super("css");
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

            if (c == '{') {
                String selector = source.substring(lastPos, scanner.getPos()).trim();
                CodeNode node = new CodeNode(NodeType.SELECTOR, selector, lastPos);
                scopeStack.peek().addChild(node);
                scopeStack.push(node);
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
            } else if (c == '/' && scanner.peekNext() == '*') {
                scopeStack.peek().addChild(extractBlockComment(scanner, "*/"));
                lastPos = scanner.getPos();
            } else if (c == ';') {
                String property = source.substring(lastPos, scanner.getPos()).trim();
                if (!property.isEmpty()) {
                    CodeNode node = new CodeNode(NodeType.PROPERTY, property, lastPos, scanner.getPos());
                    node.setContent(property);
                    scopeStack.peek().addChild(node);
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
}
