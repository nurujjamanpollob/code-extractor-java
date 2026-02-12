package com.extractor.languages;

import com.extractor.core.BaseParser;
import com.extractor.core.ParserRule;
import com.extractor.core.SourceScanner;
import com.extractor.model.CodeNode;
import com.extractor.model.NodeType;

import java.util.List;
import java.util.Optional;
import java.util.Stack;

public class PHPParser extends BaseParser {

    private final List<ParserRule> phpRules = List.of(
        (ctx, pos) -> Optional.ofNullable(extractIdentifierAfterKeyword(stripComments(ctx).trim(), "class"))
                .map(name -> new CodeNode(NodeType.CLASS, name, pos)),
        (ctx, pos) -> Optional.ofNullable(extractIdentifierAfterKeyword(stripComments(ctx).trim(), "interface"))
                .map(name -> new CodeNode(NodeType.INTERFACE, name, pos)),
        (ctx, pos) -> Optional.ofNullable(extractIdentifierAfterKeyword(stripComments(ctx).trim(), "trait"))
                .map(name -> new CodeNode(NodeType.TRAIT, name, pos)),
        (ctx, pos) -> Optional.ofNullable(extractIdentifierAfterKeyword(stripComments(ctx).trim(), "function"))
                .map(name -> new CodeNode(NodeType.METHOD, name, pos))
    );

    public PHPParser() {
        super("php");
    }

    @Override
    public CodeNode parse(String source) {
        if (source == null) return new CodeNode(NodeType.ROOT, "root", 0);

        CodeNode root = new CodeNode(NodeType.ROOT, "root", 0);
        SourceScanner scanner = new SourceScanner(source);
        Stack<CodeNode> scopeStack = new Stack<>();
        scopeStack.push(root);

        int lastPos = 0;
        while (!scanner.isAtEnd()) {
            char c = scanner.peek();
            if (c == '{') {
                String lookback = scanner.substring(lastPos, scanner.getPos());
                CodeNode node = applyRules(phpRules, lookback, lastPos)
                        .orElse(new CodeNode(NodeType.BLOCK, "anonymous", lastPos));
                
                scopeStack.peek().addChild(node);
                scopeStack.push(node);
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
            } else if (c == '"' || c == '\'') {
                skipString(scanner);
            } else if (c == '/' && scanner.peekNext() == '/') {
                scopeStack.peek().addChild(extractLineComment(scanner));
                lastPos = scanner.getPos();
            } else if (c == '/' && scanner.peekNext() == '*') {
                scopeStack.peek().addChild(extractBlockComment(scanner, "*/"));
                lastPos = scanner.getPos();
            } else if (c == '#') {
                // PHP shell-style comment
                int start = scanner.getPos();
                StringBuilder content = new StringBuilder();
                while (!scanner.isAtEnd() && scanner.peek() != '\n') {
                    content.append(scanner.advance());
                }
                CodeNode comment = new CodeNode(NodeType.COMMENT, "shell comment", start);
                comment.setContent(content.toString());
                comment.setEndOffset(scanner.getPos());
                scopeStack.peek().addChild(comment);
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
