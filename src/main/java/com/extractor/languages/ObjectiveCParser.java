package com.extractor.languages;

import com.extractor.core.BaseParser;
import com.extractor.core.SourceScanner;
import com.extractor.model.CodeNode;
import com.extractor.model.NodeType;

import java.util.Stack;

public class ObjectiveCParser extends BaseParser {
    public ObjectiveCParser() {
        super("objectivec");
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
                    CodeNode newNode = identifyObjCNode(lookback, lastPos);
                    scopeStack.peek().addChild(newNode);
                    scopeStack.push(newNode);
                    scanner.advance();
                    lastPos = scanner.getPos();
                } else if (c == '}') {
                    if (scopeStack.size() > 1) {
                        NodeType currentType = scopeStack.peek().getType();
                        // In Objective-C, @interface and @implementation are closed by @end, not }
                        if (currentType != NodeType.CLASS && currentType != NodeType.INTERFACE) {
                            CodeNode closed = scopeStack.pop();
                            closed.setEndOffset(scanner.getPos() + 1);
                            closed.setContent(source.substring(closed.getStartOffset(), Math.min(closed.getEndOffset(), source.length())));
                        }
                    }
                    scanner.advance();
                    lastPos = scanner.getPos();
                } else if (c == '\n' || c == ';') {
                    String line = scanner.substring(lastPos, scanner.getPos()).trim();
                    if (line.startsWith("@interface") || line.startsWith("@implementation")) {
                        NodeType type = line.startsWith("@interface") ? NodeType.INTERFACE : NodeType.CLASS;
                        String name = extractIdentifierAfterKeyword(line, line.startsWith("@interface") ? "@interface" : "@implementation");
                        if (name != null) {
                            if (name.contains(":")) name = name.split(":")[0].trim();
                            
                            CodeNode newNode = new CodeNode(type, name, lastPos);
                            scopeStack.peek().addChild(newNode);
                            scopeStack.push(newNode);
                        }
                    } else if (line.startsWith("@end")) {
                        // Pop until we find a CLASS or INTERFACE
                        while (scopeStack.size() > 1) {
                            NodeType type = scopeStack.peek().getType();
                            if (type == NodeType.CLASS || type == NodeType.INTERFACE) {
                                CodeNode closed = scopeStack.pop();
                                closed.setEndOffset(scanner.getPos());
                                closed.setContent(source.substring(closed.getStartOffset(), Math.min(closed.getEndOffset(), source.length())));
                                break;
                            }
                            scopeStack.pop();
                        }
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

    private CodeNode identifyObjCNode(String lookback, int pos) {
        if (lookback.contains("@interface")) {
            String name = extractIdentifierAfterKeyword(lookback, "@interface");
            return new CodeNode(NodeType.INTERFACE, name != null ? name : "unknown", pos);
        }
        if (lookback.contains("@implementation")) {
            String name = extractIdentifierAfterKeyword(lookback, "@implementation");
            return new CodeNode(NodeType.CLASS, name != null ? name : "unknown", pos);
        }
        
        // Method detection: - (void)name or + (id)name
        if (lookback.contains("-") || lookback.contains("+")) {
            int bracketIndex = lookback.lastIndexOf(')');
            if (bracketIndex != -1) {
                String afterBracket = lookback.substring(bracketIndex + 1).trim();
                if (!afterBracket.isEmpty()) {
                    String name = afterBracket.split("[\\s:{]")[0];
                    return new CodeNode(NodeType.METHOD, name, pos);
                }
            }
        }

        return new CodeNode(NodeType.BLOCK, "block", pos);
    }
}
