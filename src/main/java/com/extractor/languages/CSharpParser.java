package com.extractor.languages;

import com.extractor.core.BaseParser;
import com.extractor.core.ParserRule;
import com.extractor.core.SourceScanner;
import com.extractor.model.CodeNode;
import com.extractor.model.NodeType;

import java.util.List;
import java.util.Optional;
import java.util.Stack;

public class CSharpParser extends BaseParser {

    private final List<ParserRule> csharpRules;

    public CSharpParser() {
        super("csharp");
        this.csharpRules = List.of(
            (ctx, pos) -> Optional.ofNullable(extractIdentifierAfterKeyword(stripComments(ctx).trim(), "namespace"))
                    .map(name -> new CodeNode(NodeType.NAMESPACE, name, pos)),
            (ctx, pos) -> Optional.ofNullable(extractIdentifierAfterKeyword(stripComments(ctx).trim(), "class"))
                    .map(name -> new CodeNode(NodeType.CLASS, name, pos)),
            (ctx, pos) -> Optional.ofNullable(extractIdentifierAfterKeyword(stripComments(ctx).trim(), "interface"))
                    .map(name -> new CodeNode(NodeType.INTERFACE, name, pos)),
            (ctx, pos) -> Optional.ofNullable(extractIdentifierAfterKeyword(stripComments(ctx).trim(), "struct"))
                    .map(name -> new CodeNode(NodeType.STRUCT, name, pos)),
            (ctx, pos) -> {
                String clean = stripComments(ctx).trim();
                return isMethodDeclaration(clean) ? Optional.of(new CodeNode(NodeType.METHOD, extractMethodName(clean), pos)) : Optional.empty();
            }
        );
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

            if (c == '"' || c == '\'') {
                skipString(scanner);
            } else if (c == '@' && scanner.peekNext() == '"') {
                scanner.advance();
                skipString(scanner);
            } else if (c == '/' && scanner.peekNext() == '/') {
                extractLineComment(scanner);
            } else if (c == '/' && scanner.peekNext() == '*') {
                scopeStack.peek().addChild(extractBlockComment(scanner, "*/"));
            } else if (c == '{') {
                String lookback = source.substring(lastPos, scanner.getPos());
                CodeNode node = applyRules(csharpRules, lookback, lastPos)
                        .orElse(new CodeNode(NodeType.BLOCK, "anonymous", lastPos));
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
            } else if (c == ';') {
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
