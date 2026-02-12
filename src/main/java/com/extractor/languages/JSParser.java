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

/**
 * JavaScript parser that extracts classes, methods, and functions.
 */
public class JSParser extends BaseParser {

    private final List<ParserRule> blockRules;
    private final List<ParserRule> statementRules;

    public JSParser() {
        super("javascript");
        this.blockRules = new ArrayList<>();
        this.statementRules = new ArrayList<>();
        initializeRules();
    }

    private void initializeRules() {
        // Classes - Handle class declarations including exports
        blockRules.add((ctx, pos) -> {
            String clean = stripComments(ctx).trim();
            String name = extractIdentifierAfterKeyword(clean, "class");
            return name != null ? Optional.of(new CodeNode(NodeType.CLASS, name, pos)) : Optional.empty();
        });

        // Control Structures - Avoid misidentifying them as functions
        blockRules.add((ctx, pos) -> {
            String clean = stripComments(ctx).trim();
            for (String keyword : List.of("if", "for", "while", "switch", "try", "catch")) {
                if (clean.startsWith(keyword)) {
                    return Optional.of(new CodeNode(NodeType.BLOCK, keyword, pos));
                }
            }
            return Optional.empty();
        });

        // Functions and Methods
        blockRules.add((ctx, pos) -> {
            String clean = stripComments(ctx).trim();
            
            // Function declaration: function name(...) or async function name(...)
            Pattern p = Pattern.compile("(?:async\\s+)?function\\s+([a-zA-Z_$][a-zA-Z0-9_$]*)");
            Matcher m = p.matcher(clean);
            if (m.find()) {
                return Optional.of(new CodeNode(NodeType.FUNCTION, m.group(1), pos));
            }

            // Arrow function assigned to variable: const name = (...) =>
            p = Pattern.compile("(?:const|let|var)\\s+([a-zA-Z_$][a-zA-Z0-9_$]*)\\s*=\\s*(?:async\\s*)?\\(");
            m = p.matcher(clean);
            if (m.find() && clean.contains("=>")) {
                return Optional.of(new CodeNode(NodeType.FUNCTION, m.group(1), pos));
            }

            // Method in class: name(...) { - Support multi-line declarations with (?s)
            boolean isMethod = clean.matches("(?s).*(?:async\\s+|static\\s+|get\\s+|set\\s+)?(?:[a-zA-Z_$][a-zA-Z0-9_$]*)\\s*\\(.*\\).*");
            if (isMethod) {
                String name = extractMethodName(clean);
                if (name != null && !isControlKeyword(name)) {
                    return Optional.of(new CodeNode(NodeType.FUNCTION, name, pos)); // Type will be adjusted in parse loop
                }
            }

            return Optional.empty();
        });
    }

    @Override
    protected String extractMethodName(String clean) {
        // Improved method name extraction to handle keywords and spacing
        Pattern p = Pattern.compile("(?:async\\s+|static\\s+|get\\s+|set\\s+)?([a-zA-Z_$][a-zA-Z0-9_$]*)\\s*\\(");
        Matcher m = p.matcher(clean);
        // Find the LAST match in case there are multiple words that look like method calls in the context
        String lastMatch = null;
        while (m.find()) {
            String candidate = m.group(1);
            if (!isControlKeyword(candidate)) {
                lastMatch = candidate;
            }
        }
        return lastMatch;
    }

    @Override
    protected boolean isControlKeyword(String name) {
        return List.of("if", "for", "while", "switch", "catch", "with", "function", "try").contains(name);
    }

    @Override
    public CodeNode parse(String source) {
        if (source == null || source.isEmpty()) return new CodeNode(NodeType.ROOT, "root", 0);
        
        SourceScanner scanner = new SourceScanner(source);
        CodeNode root = new CodeNode(NodeType.ROOT, "root", 0);
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
                
                // Adjust node type if it is a method within a class
                if (scopeStack.peek().getType() == NodeType.CLASS && node.getType() == NodeType.FUNCTION) {
                    node.setType(NodeType.METHOD);
                }

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
            } else if (c == ';') {
                // Here we could apply statementRules if needed
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

    @Override
    protected boolean isMethodDeclaration(String text) {
        // Not directly used in the current parse loop but good for consistency
        String clean = stripComments(text).trim();
        return clean.matches("(?s).*(?:async\\s+|static\\s+|get\\s+|set\\s+)?(?:[a-zA-Z_$][a-zA-Z0-9_$]*)\\s*\\(.*\\).*");
    }
}
