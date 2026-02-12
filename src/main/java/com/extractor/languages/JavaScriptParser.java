package com.extractor.languages;

import com.extractor.core.BaseParser;
import com.extractor.core.ParserRule;
import com.extractor.core.SourceScanner;
import com.extractor.model.CodeNode;
import com.extractor.model.NodeType;

import java.util.List;
import java.util.Optional;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaScriptParser extends BaseParser {

    private final List<ParserRule> blockRules;
    private final List<ParserRule> statementRules;

    public JavaScriptParser() {
        super("javascript");
        this.blockRules = List.of(
            (ctx, pos) -> {
                String clean = stripComments(ctx).trim();
                String name = extractIdentifierAfterKeyword(clean, "class");
                return name != null ? Optional.of(new CodeNode(NodeType.CLASS, name, pos)) : Optional.empty();
            },
            (ctx, pos) -> {
                String clean = stripComments(ctx).trim();
                // Function declaration: function name(...)
                Pattern p = Pattern.compile("function\\s+([a-zA-Z_][a-zA-Z0-9_]*)");
                Matcher m = p.matcher(clean);
                if (m.find()) {
                    return Optional.of(new CodeNode(NodeType.FUNCTION, m.group(1), pos));
                }
                // Async function
                p = Pattern.compile("async\\s+function\\s+([a-zA-Z_][a-zA-Z0-9_]*)");
                m = p.matcher(clean);
                if (m.find()) {
                    return Optional.of(new CodeNode(NodeType.FUNCTION, m.group(1), pos));
                }
                // Method in class
                p = Pattern.compile("^\\s*(?:async\\s+)?([a-zA-Z_][a-zA-Z0-9_]*)\\s*\\(");
                m = p.matcher(clean);
                if (m.find() && !isControlKeyword(m.group(1))) {
                    return Optional.of(new CodeNode(NodeType.METHOD, m.group(1), pos));
                }
                // Arrow function assigned to variable
                p = Pattern.compile("(?:const|let|var)\\s+([a-zA-Z_][a-zA-Z0-9_]*)\\s*=\\s*(?:async\\s*)?\\([^\\)]*\\)\\s*=>");
                m = p.matcher(clean);
                if (m.find()) {
                    return Optional.of(new CodeNode(NodeType.FUNCTION, m.group(1), pos));
                }
                return Optional.empty();
            }
        );

        this.statementRules = List.of(
            (ctx, pos) -> {
                String clean = stripComments(ctx).trim();
                if (clean.startsWith("import ")) {
                    return Optional.of(new CodeNode(NodeType.MODULE, "import", pos));
                }
                if (clean.startsWith("export ")) {
                    return Optional.of(new CodeNode(NodeType.MODULE, "export", pos));
                }
                return Optional.empty();
            }
        );
    }

    @Override
    protected boolean isControlKeyword(String name) {
        return name.equals("if") || name.equals("for") || name.equals("while") || 
               name.equals("switch") || name.equals("catch") || name.equals("with") || name.equals("function");
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

            if (c == '/' && scanner.peekNext() == '/') {
                scopeStack.peek().addChild(extractLineComment(scanner));
            } else if (c == '/' && scanner.peekNext() == '*') {
                scopeStack.peek().addChild(extractBlockComment(scanner, "*/"));
            } else if (c == '"' || c == '\'' || c == '`') {
                skipString(scanner);
            } else if (c == '{') {
                String lookback = source.substring(lastPos, scanner.getPos());
                CodeNode node = applyRules(blockRules, lookback, lastPos)
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
                String statement = source.substring(lastPos, scanner.getPos());
                applyRules(statementRules, statement, lastPos).ifPresent(node -> {
                    node.setEndOffset(scanner.getPos());
                    node.setContent(statement.trim());
                    scopeStack.peek().addChild(node);
                });
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
