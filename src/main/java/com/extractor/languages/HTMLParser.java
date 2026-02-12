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
                        // Skip DocType or other <! tags
                        while (!scanner.isAtEnd() && scanner.peek() != '>') {
                            scanner.advance();
                        }
                        if (!scanner.isAtEnd()) scanner.advance();
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
                    
                    if (scopeStack.size() > 1 && scopeStack.peek().getName().equalsIgnoreCase(tagName)) {
                        CodeNode closed = scopeStack.pop();
                        closed.setEndOffset(scanner.getPos());
                        closed.setContent(source.substring(closed.getStartOffset(), closed.getEndOffset()));
                        
                        // Handle embedded CSS or JS when the tag is closed
                        if ("style".equalsIgnoreCase(tagName)) {
                            parseEmbeddedContent(closed, new CSSParser());
                        } else if ("script".equalsIgnoreCase(tagName)) {
                            parseEmbeddedContent(closed, new JSParser());
                        }
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

    private void parseEmbeddedContent(CodeNode node, BaseParser parser) {
        String content = node.getContent();
        if (content == null) return;
        
        int openTagEnd = content.indexOf('>') + 1;
        int closeTagStart = content.lastIndexOf('<');
        
        if (openTagEnd > 0 && closeTagStart > openTagEnd) {
            String embeddedCode = content.substring(openTagEnd, closeTagStart);
            CodeNode embeddedRoot = parser.parse(embeddedCode);
            
            int offsetAdjustment = node.getStartOffset() + openTagEnd;
            for (CodeNode child : embeddedRoot.getChildren()) {
                adjustOffsets(child, offsetAdjustment);
                node.addChild(child);
            }
        }
    }

    private void adjustOffsets(CodeNode node, int adjustment) {
        node.setStartOffset(node.getStartOffset() + adjustment);
        node.setEndOffset(node.getEndOffset() + adjustment);
        for (CodeNode child : node.getChildren()) {
            adjustOffsets(child, adjustment);
        }
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
    
    @Override
    protected void finalizeScope(Stack<CodeNode> scopeStack, int endPos, String source) {
        while (scopeStack.size() > 1) {
            CodeNode node = scopeStack.pop();
            node.setEndOffset(endPos);
            node.setContent(source.substring(node.getStartOffset(), endPos));
            
            // Still try to parse embedded content if the file ends before the closing tag
            if ("style".equalsIgnoreCase(node.getName())) {
                parseEmbeddedContent(node, new CSSParser());
            } else if ("script".equalsIgnoreCase(node.getName())) {
                parseEmbeddedContent(node, new JSParser());
            }
        }
    }
}
