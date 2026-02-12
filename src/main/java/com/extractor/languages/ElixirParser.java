package com.extractor.languages;

import com.extractor.core.BaseParser;
import com.extractor.core.SourceScanner;
import com.extractor.model.CodeNode;
import com.extractor.model.NodeType;

import java.util.Optional;
import java.util.Stack;

public class ElixirParser extends BaseParser {

    public ElixirParser() {
        super("elixir");
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

            if (c == '"') {
                skipString(scanner);
            } else if (c == '#') {
                extractLineComment(scanner);
            } else {
                String lookahead3 = scanner.peek(3);
                if (lookahead3.equals("do ") || lookahead3.equals("do\n")) {
                    String lookback = scanner.substring(lastPos, scanner.getPos());
                    CodeNode node = identifyElixirNode(lookback, lastPos);
                    scopeStack.peek().addChild(node);
                    scopeStack.push(node);
                    scanner.advance(2);
                    lastPos = scanner.getPos();
                } else if (scanner.peek(4).equals("end ") || scanner.peek(4).equals("end\n") || (scanner.peek(3).equals("end") && (scanner.getPos() + 3 == source.length()))) {
                    if (scopeStack.size() > 1) {
                        CodeNode closed = scopeStack.pop();
                        closed.setEndOffset(scanner.getPos() + 3);
                        closed.setContent(source.substring(closed.getStartOffset(), Math.min(closed.getEndOffset(), source.length())));
                    }
                    scanner.advance(3);
                    lastPos = scanner.getPos();
                } else {
                    scanner.advance();
                }
            }
        }

        root.setEndOffset(source.length());
        root.setContent(source);
        return root;
    }

    private CodeNode identifyElixirNode(String lookback, int pos) {
        lookback = stripComments(lookback).trim();
        if (lookback.contains("defmodule")) {
            return new CodeNode(NodeType.MODULE, extractIdentifierAfterKeyword(lookback, "defmodule"), pos);
        }
        if (lookback.contains("defp")) {
            return new CodeNode(NodeType.FUNCTION, extractIdentifierAfterKeyword(lookback, "defp"), pos);
        }
        if (lookback.contains("def")) {
            return new CodeNode(NodeType.FUNCTION, extractIdentifierAfterKeyword(lookback, "def"), pos);
        }
        return new CodeNode(NodeType.BLOCK, "do-block", pos);
    }
}
