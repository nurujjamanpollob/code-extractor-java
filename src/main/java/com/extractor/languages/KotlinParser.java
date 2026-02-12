package com.extractor.languages;

import com.extractor.core.BaseParser;
import com.extractor.core.SourceScanner;
import com.extractor.model.CodeNode;
import com.extractor.model.NodeType;

import java.util.Stack;

public class KotlinParser extends BaseParser {
    public KotlinParser() {
        super("kotlin");
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
                    CodeNode newNode = identifyKotlinNode(lookback, lastPos, scopeStack.peek());
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
                } else if (c == '\n' || c == ';') {
                    String line = scanner.substring(lastPos, scanner.getPos()).trim();
                    if (!line.isEmpty()) {
                        processLine(line, lastPos, scanner.getPos(), scopeStack.peek());
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
            // Log or handle exception
        }

        finalizeScope(scopeStack, source.length(), source);
        root.setEndOffset(source.length());
        root.setContent(source);
        return root;
    }

    private void processLine(String line, int start, int end, CodeNode parent) {
        if (line.startsWith("package ") || line.startsWith("import ")) {
            String keyword = line.startsWith("package") ? "package" : "import";
            String name = extractIdentifierAfterKeyword(line, keyword);
            if (name != null) {
                CodeNode node = new CodeNode(NodeType.MODULE, name, start);
                node.setEndOffset(end);
                node.setContent(line);
                parent.addChild(node);
            }
            return;
        }

        // Check for bodyless declarations
        String className = extractIdentifierAfterKeyword(line, "class");
        if (className != null) {
            addNode(parent, NodeType.CLASS, className, start, end, line);
            return;
        }

        String interfaceName = extractIdentifierAfterKeyword(line, "interface");
        if (interfaceName != null) {
            addNode(parent, NodeType.INTERFACE, interfaceName, start, end, line);
            return;
        }

        String funName = extractIdentifierAfterKeyword(line, "fun");
        if (funName != null) {
            NodeType type = (parent.getType() == NodeType.ROOT) ? NodeType.FUNCTION : NodeType.METHOD;
            addNode(parent, type, funName, start, end, line);
            return;
        }

        String valName = extractIdentifierAfterKeyword(line, "val");
        if (valName != null) {
            addNode(parent, NodeType.VARIABLE, valName, start, end, line);
            return;
        }

        String varName = extractIdentifierAfterKeyword(line, "var");
        if (varName != null) {
            addNode(parent, NodeType.VARIABLE, varName, start, end, line);
        }
    }

    private void addNode(CodeNode parent, NodeType type, String name, int start, int end, String content) {
        CodeNode node = new CodeNode(type, name, start);
        node.setEndOffset(end);
        node.setContent(content);
        parent.addChild(node);
    }

    private CodeNode identifyKotlinNode(String lookback, int pos, CodeNode parent) {
        String className = extractIdentifierAfterKeyword(lookback, "class");
        if (className != null) return new CodeNode(NodeType.CLASS, className, pos);

        String interfaceName = extractIdentifierAfterKeyword(lookback, "interface");
        if (interfaceName != null) return new CodeNode(NodeType.INTERFACE, interfaceName, pos);

        String objectName = extractIdentifierAfterKeyword(lookback, "object");
        if (objectName != null) return new CodeNode(NodeType.CLASS, objectName, pos);

        String funName = extractIdentifierAfterKeyword(lookback, "fun");
        if (funName != null) {
            NodeType type = (parent == null || parent.getType() == NodeType.ROOT) ? NodeType.FUNCTION : NodeType.METHOD;
            return new CodeNode(type, funName, pos);
        }

        return new CodeNode(NodeType.BLOCK, "anonymous", pos);
    }
}
