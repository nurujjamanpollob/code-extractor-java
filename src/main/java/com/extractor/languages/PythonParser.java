package com.extractor.languages;

import com.extractor.core.BaseParser;
import com.extractor.model.CodeNode;
import com.extractor.model.NodeType;

import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PythonParser extends BaseParser {

    private static final Pattern CLASS_PATTERN = Pattern.compile("^\\s*class\\s+([a-zA-Z_][a-zA-Z0-9_]*)");
    private static final Pattern FUNC_PATTERN = Pattern.compile("^\\s*def\\s+([a-zA-Z_][a-zA-Z0-9_]*)");
    private static final Pattern DECORATOR_PATTERN = Pattern.compile("^\\s*@([a-zA-Z_][a-zA-Z0-9_.]*)");

    public PythonParser() {
        super("python");
    }

    @Override
    public CodeNode parse(String source) {
        CodeNode root = new CodeNode(NodeType.ROOT, "root", 0);
        if (source == null || source.isEmpty()) return root;

        // Normalize line endings to simplify offset calculation
        String normalizedSource = source.replace("\r\n", "\n");
        String[] lines = normalizedSource.split("\n", -1);
        
        Stack<CodeNode> scopeStack = new Stack<>();
        scopeStack.push(root);
        
        Stack<Integer> indentStack = new Stack<>();
        indentStack.push(-1);

        int currentPos = 0;
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            String trimmed = line.trim();
            int lineStartPos = currentPos;
            
            // Update currentPos for next iteration (using normalized newline length of 1)
            currentPos += line.length() + 1;

            if (trimmed.isEmpty()) {
                continue;
            }

            int indentation = getIndentation(line);

            // Pop scopes that have higher or equal indentation
            // A non-empty line with less or equal indentation ends the previous scope
            while (indentStack.peek() >= indentation && scopeStack.size() > 1) {
                CodeNode closed = scopeStack.pop();
                closed.setEndOffset(lineStartPos > 0 ? lineStartPos - 1 : 0);
                closed.setContent(normalizedSource.substring(closed.getStartOffset(), closed.getEndOffset()));
                indentStack.pop();
            }

            // Handle comments
            if (trimmed.startsWith("#")) {
                CodeNode comment = new CodeNode(NodeType.COMMENT, "comment", lineStartPos + line.indexOf("#"));
                comment.setEndOffset(lineStartPos + line.length());
                comment.setContent(trimmed);
                scopeStack.peek().addChild(comment);
                continue;
            }

            // Handle docstrings
            if (trimmed.startsWith("\"\"\"") || trimmed.startsWith("'''")) {
                String quote = trimmed.substring(0, 3);
                int start = lineStartPos + line.indexOf(quote);
                StringBuilder content = new StringBuilder(trimmed);
                if (!trimmed.endsWith(quote) || trimmed.length() == 3) {
                    i++;
                    while (i < lines.length) {
                        String nextLine = lines[i];
                        content.append("\n").append(nextLine);
                        currentPos += nextLine.length() + 1;
                        if (nextLine.contains(quote)) {
                            break;
                        }
                        i++;
                    }
                }
                CodeNode docNode = new CodeNode(NodeType.COMMENT, "docstring", start);
                docNode.setEndOffset(Math.min(currentPos - 1, normalizedSource.length()));
                docNode.setContent(content.toString());
                scopeStack.peek().addChild(docNode);
                continue;
            }

            // Check for decorators
            Matcher decMatcher = DECORATOR_PATTERN.matcher(line);
            if (decMatcher.find()) {
                CodeNode decNode = new CodeNode(NodeType.ANNOTATION, decMatcher.group(1), lineStartPos + decMatcher.start());
                decNode.setEndOffset(lineStartPos + line.length());
                decNode.setContent(trimmed);
                scopeStack.peek().addChild(decNode);
                continue;
            }

            // Check for class or function
            Matcher classMatcher = CLASS_PATTERN.matcher(line);
            Matcher funcMatcher = FUNC_PATTERN.matcher(line);

            if (classMatcher.find()) {
                CodeNode node = new CodeNode(NodeType.CLASS, classMatcher.group(1), lineStartPos);
                scopeStack.peek().addChild(node);
                scopeStack.push(node);
                indentStack.push(indentation);
            } else if (funcMatcher.find()) {
                NodeType type = scopeStack.peek().getType() == NodeType.CLASS ? NodeType.METHOD : NodeType.FUNCTION;
                CodeNode node = new CodeNode(type, funcMatcher.group(1), lineStartPos);
                scopeStack.peek().addChild(node);
                scopeStack.push(node);
                indentStack.push(indentation);
            }
        }

        // Close remaining scopes
        while (scopeStack.size() > 1) {
            CodeNode closed = scopeStack.pop();
            closed.setEndOffset(normalizedSource.length());
            closed.setContent(normalizedSource.substring(closed.getStartOffset(), normalizedSource.length()));
        }

        root.setEndOffset(normalizedSource.length());
        root.setContent(normalizedSource);
        return root;
    }

    private int getIndentation(String line) {
        int count = 0;
        for (char c : line.toCharArray()) {
            if (c == ' ') count++;
            else if (c == '\t') count += 4;
            else break;
        }
        return count;
    }

    @Override
    public boolean supports(String language) {
        return "python".equalsIgnoreCase(language);
    }
}
