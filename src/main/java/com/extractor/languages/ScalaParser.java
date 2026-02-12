package com.extractor.languages;

import com.extractor.core.BaseParser;
import com.extractor.core.SourceScanner;
import com.extractor.model.CodeNode;
import com.extractor.model.NodeType;

import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for Scala source code.
 * Handles classes, objects, traits, and methods (including those without braces).
 */
public class ScalaParser extends BaseParser {

    public ScalaParser() {
        super("scala");
    }

    @Override
    public CodeNode parse(String source) {
        CodeNode root = new CodeNode(NodeType.ROOT, "root", 0);
        if (source == null || source.isEmpty()) return root;

        SourceScanner scanner = new SourceScanner(source);
        Stack<CodeNode> scopeStack = new Stack<>();
        scopeStack.push(root);

        int lastPos = 0;

        try {
            while (!scanner.isAtEnd()) {
                char c = scanner.peek();

                if (c == '{') {
                    String lookback = scanner.substring(lastPos, scanner.getPos());
                    CodeNode newNode = identifyScalaNode(lookback, lastPos);
                    scopeStack.peek().addChild(newNode);
                    scopeStack.push(newNode);
                    scanner.advance();
                    lastPos = scanner.getPos();
                } else if (c == '}') {
                    if (scopeStack.size() > 1) {
                        CodeNode closed = scopeStack.pop();
                        closed.setEndOffset(Math.min(scanner.getPos() + 1, source.length()));
                        closed.setContent(source.substring(closed.getStartOffset(), closed.getEndOffset()));
                    }
                    scanner.advance();
                    lastPos = scanner.getPos();
                } else if (c == '"') {
                    if (scanner.match("\"\"\"")) {
                        while (!scanner.isAtEnd() && !scanner.match("\"\"\"")) {
                            scanner.advance();
                        }
                    } else {
                        skipString(scanner);
                    }
                    // Note: We do NOT update lastPos here to maintain context for declarations
                    // that might include strings (e.g., default parameter values).
                } else if (c == '/' && scanner.peekNext() == '/') {
                    scopeStack.peek().addChild(extractLineComment(scanner));
                    // Note: We do NOT update lastPos here.
                } else if (c == '/' && scanner.peekNext() == '*') {
                    scopeStack.peek().addChild(extractBlockComment(scanner, "*/"));
                    // Note: We do NOT update lastPos here.
                } else if (c == '\n' || c == ';') {
                    String rawLine = scanner.substring(lastPos, scanner.getPos());
                    String line = stripComments(rawLine).trim();
                    
                    if (line.startsWith("package ") || line.startsWith("import ")) {
                        NodeType type = line.startsWith("package") ? NodeType.MODULE : NodeType.MODULE;
                        String keyword = line.startsWith("package") ? "package" : "import";
                        String name = extractIdentifierAfterKeyword(line, keyword);
                        if (name != null) {
                            CodeNode node = new CodeNode(type, name, lastPos);
                            node.setEndOffset(scanner.getPos());
                            node.setContent(rawLine.trim());
                            scopeStack.peek().addChild(node);
                        }
                    } else if (line.contains("def ") && !line.contains("{")) {
                        String name = extractScalaMethodName(line);
                        if (!"unknown".equals(name)) {
                            CodeNode node = new CodeNode(NodeType.METHOD, name, lastPos);
                            node.setEndOffset(scanner.getPos());
                            node.setContent(rawLine.trim());
                            scopeStack.peek().addChild(node);
                        }
                    }
                    scanner.advance();
                    lastPos = scanner.getPos();
                } else {
                    scanner.advance();
                }
            }
        } catch (Exception e) {
            // Fallback for unexpected errors during parsing
        }

        finalizeScope(scopeStack, source.length(), source);
        root.setEndOffset(source.length());
        root.setContent(source);
        return root;
    }

    private CodeNode identifyScalaNode(String lookback, int pos) {
        String clean = stripComments(lookback);
        if (clean.contains("class ")) return new CodeNode(NodeType.CLASS, extractIdentifierAfterKeyword(clean, "class"), pos);
        if (clean.contains("object ")) return new CodeNode(NodeType.CLASS, extractIdentifierAfterKeyword(clean, "object"), pos);
        if (clean.contains("trait ")) return new CodeNode(NodeType.TRAIT, extractIdentifierAfterKeyword(clean, "trait"), pos);
        if (clean.contains("def ")) return new CodeNode(NodeType.METHOD, extractScalaMethodName(clean), pos);
        
        return new CodeNode(NodeType.BLOCK, "anonymous", pos);
    }

    private String extractScalaMethodName(String text) {
        // Match def followed by whitespace, then the method name (identifier or operator)
        // Stops at whitespace, (, [, {, or :
        Pattern pattern = Pattern.compile("\\bdef\\s+([^\\s\\(\\)\\[\\]\\{\\}:]+)");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "unknown";
    }
}
