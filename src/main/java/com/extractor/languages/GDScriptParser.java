package com.extractor.languages;

import com.extractor.core.BaseParser;
import com.extractor.core.SourceScanner;
import com.extractor.model.CodeNode;
import com.extractor.model.NodeType;

import java.util.Stack;

public class GDScriptParser extends BaseParser {

    public GDScriptParser() {
        super("gdscript");
    }

    private CodeNode identifyGDScriptNode(String line, int lineStartOffset) {
        String trimmed = line.trim();
        if (trimmed.startsWith("class ")) {
            return new CodeNode(NodeType.CLASS, extractIdentifierAfterKeyword(trimmed, "class"), lineStartOffset);
        }
        if (trimmed.startsWith("static func ")) {
            return new CodeNode(NodeType.FUNCTION, extractIdentifierAfterKeyword(trimmed, "func"), lineStartOffset);
        }
        if (trimmed.startsWith("func ")) {
            return new CodeNode(NodeType.FUNCTION, extractIdentifierAfterKeyword(trimmed, "func"), lineStartOffset);
        }
        if (trimmed.startsWith("var ")) {
            return new CodeNode(NodeType.VARIABLE, extractIdentifierAfterKeyword(trimmed, "var"), lineStartOffset);
        }
        if (trimmed.startsWith("const ")) {
            return new CodeNode(NodeType.VARIABLE, extractIdentifierAfterKeyword(trimmed, "const"), lineStartOffset);
        }
        if (trimmed.startsWith("enum ")) {
            return new CodeNode(NodeType.ENUM, extractIdentifierAfterKeyword(trimmed, "enum"), lineStartOffset);
        }
        if (trimmed.startsWith("signal ")) {
            return new CodeNode(NodeType.VARIABLE, extractIdentifierAfterKeyword(trimmed, "signal"), lineStartOffset);
        }
        return null;
    }

    @Override
    public CodeNode parse(String source) {
        CodeNode root = new CodeNode(NodeType.ROOT, "root", 0);
        if (source == null || source.isEmpty()) return root;

        String[] lines = source.split("\\r?\\n");
        Stack<CodeNode> scopeStack = new Stack<>();
        Stack<Integer> indentStack = new Stack<>();
        
        scopeStack.push(root);
        indentStack.push(-1);

        int currentOffset = 0;
        for (String line : lines) {
            int lineStartOffset = currentOffset;
            currentOffset += line.length() + 1; // +1 for newline

            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;

            int indent = 0;
            while (indent < line.length() && (line.charAt(indent) == ' ' || line.charAt(indent) == '\t')) {
                indent++;
            }

            while (indentStack.peek() >= indent && scopeStack.size() > 1) {
                CodeNode closed = scopeStack.pop();
                indentStack.pop();
                closed.setEndOffset(lineStartOffset);
                closed.setContent(source.substring(closed.getStartOffset(), lineStartOffset));
            }

            CodeNode node = identifyGDScriptNode(line, lineStartOffset);
            if (node != null) {
                scopeStack.peek().addChild(node);
                if (line.trim().endsWith(":")) {
                    scopeStack.push(node);
                    indentStack.push(indent);
                }
            }
        }

        while (scopeStack.size() > 1) {
            CodeNode closed = scopeStack.pop();
            closed.setEndOffset(source.length());
            closed.setContent(source.substring(closed.getStartOffset(), source.length()));
        }

        root.setEndOffset(source.length());
        root.setContent(source);
        return root;
    }
}
