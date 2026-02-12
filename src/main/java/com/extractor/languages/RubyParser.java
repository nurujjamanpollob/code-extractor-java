package com.extractor.languages;

import com.extractor.core.BaseParser;
import com.extractor.core.SourceScanner;
import com.extractor.model.CodeNode;
import com.extractor.model.NodeType;

import java.util.Stack;

public class RubyParser extends BaseParser {
    public RubyParser() {
        super("ruby");
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

                // Check for keywords that start a block
                if (isBlockStart(scanner)) {
                    String lookback = scanner.substring(lastPos, scanner.getPos()).trim();
                    CodeNode newNode = identifyNodeRuby(lookback, scanner.getPos());
                    scopeStack.peek().addChild(newNode);
                    scopeStack.push(newNode);
                    lastPos = scanner.getPos();
                } else if (scanner.match("end") && !Character.isLetterOrDigit(scanner.peek())) {
                    if (scopeStack.size() > 1) {
                        CodeNode closed = scopeStack.pop();
                        closed.setEndOffset(scanner.getPos());
                        closed.setContent(source.substring(closed.getStartOffset(), closed.getEndOffset()));
                    }
                    lastPos = scanner.getPos();
                } else if (c == '"' || c == '\'') {
                    skipString(scanner);
                } else if (c == '#') {
                    scopeStack.peek().addChild(extractLineComment(scanner));
                    lastPos = scanner.getPos();
                } else {
                    scanner.advance();
                }
            }
        } catch (Exception e) {
            // Error resilience
        }

        finalizeScope(scopeStack, source.length(), source);
        root.setEndOffset(source.length());
        return root;
    }

    private boolean isBlockStart(SourceScanner scanner) {
        String[] keywords = {"class ", "module ", "def ", "if ", "unless ", "while ", "until ", "for ", "do "};
        for (String kw : keywords) {
            if (scanner.match(kw)) return true;
        }
        return false;
    }

    private CodeNode identifyNodeRuby(String lookback, int pos) {
        if (lookback.contains("class ")) {
            return new CodeNode(NodeType.CLASS, extractIdentifierAfterKeyword(lookback, "class"), pos);
        }
        if (lookback.contains("module ")) {
            return new CodeNode(NodeType.MODULE, extractIdentifierAfterKeyword(lookback, "module"), pos);
        }
        if (lookback.contains("def ")) {
            return new CodeNode(NodeType.METHOD, extractMethodName(lookback), pos);
        }
        return new CodeNode(NodeType.BLOCK, "anonymous", pos);
    }
}
