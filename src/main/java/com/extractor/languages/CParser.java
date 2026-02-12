package com.extractor.languages;

import com.extractor.core.BaseParser;
import com.extractor.core.ParserRule;
import com.extractor.core.SourceScanner;
import com.extractor.model.CodeNode;
import com.extractor.model.NodeType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CParser extends BaseParser {

    protected final List<ParserRule> cRules;
    // Improved pattern to handle newlines, C++ qualified names, and various return types/modifiers
    private static final Pattern METHOD_PATTERN = Pattern.compile("(?s)\\b(?:inline|static|virtual|void|int|double|float|char|auto|long|short|signed|unsigned|bool|[a-zA-Z_][a-zA-Z0-9_]*)\\s+([a-zA-Z_][a-zA-Z0-9_:]*)\\s*\\(");

    public CParser() {
        this("c");
    }

    protected CParser(String language) {
        super(language);
        this.cRules = createRules();
    }

    protected List<ParserRule> createRules() {
        List<ParserRule> rules = new ArrayList<>();
        rules.add((ctx, pos) -> Optional.ofNullable(extractIdentifierAfterKeyword(stripComments(ctx).trim(), "struct"))
                .map(name -> new CodeNode(NodeType.STRUCT, name, pos)));
        rules.add((ctx, pos) -> Optional.ofNullable(extractIdentifierAfterKeyword(stripComments(ctx).trim(), "enum"))
                .map(name -> new CodeNode(NodeType.ENUM, name, pos)));
        rules.add((ctx, pos) -> {
            String clean = stripComments(ctx).trim();
            if (isMethodDeclaration(clean)) {
                String name = extractMethodName(clean);
                return Optional.of(new CodeNode(NodeType.FUNCTION, name, pos));
            }
            return Optional.empty();
        });
        return rules;
    }

    @Override
    protected boolean isMethodDeclaration(String text) {
        String clean = stripComments(text).trim();
        if (clean.isEmpty()) return false;
        // Avoid matching class/struct/namespace declarations as methods
        if (clean.contains("class ") || clean.contains("struct ") || clean.contains("namespace ")) {
            // But only if they are not just part of a larger name (though unlikely)
            // A better check would be seeing if 'class' is at the start or after a boundary
            if (clean.matches("(?s).*\\b(class|struct|namespace)\\s+[a-zA-Z_].*")) {
                return false;
            }
        }
        
        Matcher matcher = METHOD_PATTERN.matcher(clean);
        return matcher.find();
    }

    @Override
    protected String extractMethodName(String text) {
        Matcher matcher = METHOD_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
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
            } else if (c == '/' && scanner.peekNext() == '/') {
                scopeStack.peek().addChild(extractLineComment(scanner));
            } else if (c == '/' && scanner.peekNext() == '*') {
                scopeStack.peek().addChild(extractBlockComment(scanner, "*/"));
            } else if (c == '{') {
                String lookback = source.substring(lastPos, scanner.getPos());
                CodeNode node = applyRules(cRules, lookback, lastPos)
                        .orElse(new CodeNode(NodeType.BLOCK, "anonymous", lastPos));
                
                // Adjust NodeType based on scope
                if (node.getType() == NodeType.FUNCTION) {
                    if (isInsideClass(scopeStack)) {
                        node.setType(NodeType.METHOD);
                    }
                }
                
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

    private boolean isInsideClass(Stack<CodeNode> scopeStack) {
        for (int i = scopeStack.size() - 1; i >= 0; i--) {
            NodeType type = scopeStack.get(i).getType();
            if (type == NodeType.CLASS || type == NodeType.STRUCT) {
                return true;
            }
        }
        return false;
    }
}
