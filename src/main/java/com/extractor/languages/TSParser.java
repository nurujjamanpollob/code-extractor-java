package com.extractor.languages;

import com.extractor.core.BaseParser;
import com.extractor.core.ParserRule;
import com.extractor.core.SourceScanner;
import com.extractor.model.CodeNode;
import com.extractor.model.NodeType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Enhanced TypeScript parser with support for methods, variables, and various TS-specific constructs.
 */
public class TSParser extends BaseParser {

    private static final Set<String> RESERVED_KEYWORDS = Set.of(
            "if", "else", "for", "while", "do", "switch", "case", "default",
            "break", "continue", "return", "throw", "try", "catch", "finally",
            "new", "delete", "typeof", "instanceof", "void", "yield", "await",
            "import", "export", "from", "as", "class", "interface", "enum",
            "extends", "implements", "public", "private", "protected", "static",
            "readonly", "async", "type", "namespace", "declare", "module"
    );

    private final List<ParserRule> blockRules = new ArrayList<>();
    private final List<ParserRule> statementRules = new ArrayList<>();

    public TSParser() {
        super("typescript");
        // Rules for blocks starting with '{'
        blockRules.add((ctx, pos) -> {
            String clean = stripComments(ctx).trim();
            
            // 1. Classes, Interfaces, Enums
            if (clean.contains("class ")) {
                String name = extractIdentifierAfterKeyword(clean, "class");
                if (name != null) return Optional.of(new CodeNode(NodeType.CLASS, name, pos));
            }
            if (clean.contains("interface ")) {
                String name = extractIdentifierAfterKeyword(clean, "interface");
                if (name != null) return Optional.of(new CodeNode(NodeType.INTERFACE, name, pos));
            }
            if (clean.contains("enum ")) {
                String name = extractIdentifierAfterKeyword(clean, "enum");
                if (name != null) return Optional.of(new CodeNode(NodeType.ENUM, name, pos));
            }
            
            // 2. Functions and Arrow Functions (which often precede a block)
            if (clean.contains("function") || clean.contains("=>")) {
                String name = null;
                if (clean.contains("function ")) {
                    name = extractIdentifierAfterKeyword(clean, "function");
                } else {
                    // Try to find variable name for arrow function: const foo = () => {
                    if (clean.contains("const ")) name = extractIdentifierAfterKeyword(clean, "const");
                    else if (clean.contains("let ")) name = extractIdentifierAfterKeyword(clean, "let");
                    else if (clean.contains("var ")) name = extractIdentifierAfterKeyword(clean, "var");
                    else {
                        // Might be a class property: addUser = (user: T) => {
                        Pattern p = Pattern.compile("([a-zA-Z_][a-zA-Z0-9_]*)\\s*=");
                        Matcher m = p.matcher(clean);
                        if (m.find()) name = m.group(1);
                    }
                }
                
                if (name == null) name = "anonymous";
                return Optional.of(new CodeNode(NodeType.FUNCTION, name, pos));
            }
            
            // 3. Methods (class members)
            if (isMethodDeclaration(clean)) {
                String name = extractMethodName(clean);
                if (name != null && !RESERVED_KEYWORDS.contains(name) && !"unknown".equals(name)) {
                    return Optional.of(new CodeNode(NodeType.METHOD, name, pos));
                }
            }

            // 4. Variables followed by an object literal or block: const admin: User = { ... }
            String varKeyword = null;
            if (clean.contains("const ")) varKeyword = "const ";
            else if (clean.contains("let ")) varKeyword = "let ";
            else if (clean.contains("var ")) varKeyword = "var ";
            
            if (varKeyword != null) {
                String name = extractIdentifierAfterKeyword(clean, varKeyword.trim());
                if (name != null && !"unknown".equals(name)) {
                    return Optional.of(new CodeNode(NodeType.VARIABLE, name, pos));
                }
            }
            
            return Optional.empty();
        });

        // Rules for statements ending with ';' or '\n'
        statementRules.add((ctx, pos) -> {
            String clean = stripComments(ctx).trim();
            if (clean.startsWith("import ") || clean.startsWith("export ")) {
                return Optional.of(new CodeNode(NodeType.IMPORT, "import/export", pos));
            }
            return Optional.empty();
        });

        statementRules.add((ctx, pos) -> {
            String clean = stripComments(ctx).trim();
            String keyword = null;
            if (clean.contains("const ")) keyword = "const ";
            else if (clean.contains("let ")) keyword = "let ";
            else if (clean.contains("var ")) keyword = "var ";
            
            if (keyword != null && !clean.contains("=>") && !clean.contains("function")) {
                String name = extractIdentifierAfterKeyword(clean, keyword.trim());
                if (name != null && !"unknown".equals(name)) {
                    return Optional.of(new CodeNode(NodeType.VARIABLE, name, pos));
                }
            }
            return Optional.empty();
        });
    }

    @Override
    public CodeNode parse(String source) {
        CodeNode root = new CodeNode(NodeType.ROOT, "root", 0);
        root.setContent(source);
        Stack<CodeNode> scopeStack = new Stack<>();
        scopeStack.push(root);

        SourceScanner scanner = new SourceScanner(source);
        int lastPos = 0;

        while (!scanner.isAtEnd()) {
            char c = scanner.peek();

            // Skip comments
            if (c == '/' && scanner.peekNext() == '/') {
                scanner.advance(2);
                while (!scanner.isAtEnd() && scanner.peek() != '\n') {
                    scanner.advance();
                }
            } else if (c == '/' && scanner.peekNext() == '*') {
                scanner.advance(2);
                while (!scanner.isAtEnd() && !(scanner.peek() == '*' && scanner.peekNext() == '/')) {
                    scanner.advance();
                }
                if (!scanner.isAtEnd()) {
                    scanner.advance(2);
                }
            } else if (c == '"' || c == '\'' || c == '`') {
                skipString(scanner);
            } else if (c == '{') {
                String lookback = scanner.substring(lastPos, scanner.getPos());
                CodeNode node = applyRules(blockRules, lookback, lastPos)
                        .orElse(new CodeNode(NodeType.BLOCK, "anonymous", lastPos));
                
                scopeStack.peek().addChild(node);
                scopeStack.push(node);
                scanner.advance();
                lastPos = scanner.getPos();
            } else if (c == '}') {
                if (scopeStack.size() > 1) {
                    CodeNode node = scopeStack.pop();
                    node.setEndOffset(scanner.getPos() + 1);
                    node.setContent(source.substring(node.getStartOffset(), node.getEndOffset()));
                }
                scanner.advance();
                lastPos = scanner.getPos();
            } else if (c == ';' || c == '\n') {
                String statement = scanner.substring(lastPos, scanner.getPos());
                applyRules(statementRules, statement, lastPos).ifPresent(node -> {
                    node.setEndOffset(scanner.getPos());
                    node.setContent(source.substring(node.getStartOffset(), node.getEndOffset()));
                    scopeStack.peek().addChild(node);
                });
                scanner.advance();
                lastPos = scanner.getPos();
            } else {
                scanner.advance();
            }
        }
        
        finalizeScope(scopeStack, scanner.getPos(), source);
        return root;
    }

    @Override
    protected boolean isMethodDeclaration(String text) {
        // Improved pattern for TypeScript methods, including generics, access modifiers and optional parameters.
        // Matches "methodName(", "methodName<T>(", "public methodName(", etc.
        return text.matches("(?:^|.*\\s+)[a-zA-Z_][a-zA-Z0-9_]*\\s*(?:<.*>)?\\s*\\(.*\\).*");
    }

    @Override
    protected String extractMethodName(String text) {
        // Handle methods with generics: methodName<T>(...)
        Pattern pattern = Pattern.compile("([a-zA-Z_][a-zA-Z0-9_]*)\\s*(?:<.*>)?\\s*\\(");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "unknown";
    }
}
