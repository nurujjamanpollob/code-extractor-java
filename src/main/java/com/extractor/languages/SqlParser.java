package com.extractor.languages;

import com.extractor.core.BaseParser;
import com.extractor.core.SourceScanner;
import com.extractor.model.CodeNode;
import com.extractor.model.NodeType;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SqlParser extends BaseParser {
    private static final List<String> SQL_KEYWORDS = Arrays.asList(
            "SELECT", "INSERT", "UPDATE", "DELETE", "CREATE", "DROP", "ALTER",
            "TRUNCATE", "GRANT", "REVOKE", "BEGIN", "COMMIT", "ROLLBACK", "WITH"
    );

    // SQL specific patterns to handle optional clauses like IF EXISTS and multiple table/entity keywords
    private static final Pattern TABLE_PATTERN = Pattern.compile(
            "\\b(TABLE|FROM|JOIN|UPDATE|INTO)\\s+(?:IF\\s+(?:NOT\\s+)?EXISTS\\s+)?([a-zA-Z0-9_$.]+)", 
            Pattern.CASE_INSENSITIVE);
    
    private static final Pattern PROC_PATTERN = Pattern.compile(
            "\\b(PROCEDURE|FUNCTION)\\s+(?:IF\\s+(?:NOT\\s+)?EXISTS\\s+)?([a-zA-Z0-9_$.]+)", 
            Pattern.CASE_INSENSITIVE);

    public SqlParser() {
        super("sql");
    }

    @Override
    public CodeNode parse(String source) {
        CodeNode root = new CodeNode(NodeType.ROOT, "sql-root", 0);
        if (source == null || source.isEmpty()) {
            root.setEndOffset(0);
            return root;
        }

        SourceScanner scanner = new SourceScanner(source);
        
        try {
            while (!scanner.isAtEnd()) {
                scanner.skipWhitespace();
                if (scanner.isAtEnd()) break;

                int start = scanner.getPos();
                char c = scanner.peek();
                
                if (c == '-' && scanner.peekNext() == '-') {
                    CodeNode comment = extractLineComment(scanner);
                    root.addChild(comment);
                } else if (c == '/' && scanner.peekNext() == '*') {
                    CodeNode comment = extractBlockComment(scanner, "*/");
                    root.addChild(comment);
                } else if (c == '\'' || c == '"') {
                    skipString(scanner);
                } else {
                    int end = findStatementEnd(source, scanner.getPos());
                    String statementText = source.substring(start, end).trim();
                    
                    if (!statementText.isEmpty()) {
                        String firstWord = getFirstWord(statementText).toUpperCase();
                        if (SQL_KEYWORDS.contains(firstWord)) {
                            // CTEs starting with WITH are treated as SELECT statements for extraction purposes
                            String nodeName = "WITH".equals(firstWord) ? "SELECT" : firstWord;
                            CodeNode stmtNode = new CodeNode(NodeType.STATEMENT, nodeName, start);
                            stmtNode.setEndOffset(end);
                            stmtNode.setContent(statementText);
                            
                            identifySqlEntities(stmtNode, statementText, start);
                            root.addChild(stmtNode);
                        }
                    }
                    scanner.setPos(end);
                    if (!scanner.isAtEnd() && scanner.peek() == ';') {
                        scanner.advance();
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing SQL source: " + e.getMessage());
        }

        root.setEndOffset(source.length());
        root.setContent(source);
        return root;
    }

    private String getFirstWord(String text) {
        int i = 0;
        while (i < text.length() && !Character.isWhitespace(text.charAt(i))) {
            i++;
        }
        return text.substring(0, i);
    }

    private void identifySqlEntities(CodeNode stmtNode, String text, int baseOffset) {
        // Use regex to find table, procedure, or function names, handling IF EXISTS clauses
        // Changed to while loops and removed else to find all relevant entities in a statement
        Matcher tableMatcher = TABLE_PATTERN.matcher(text);
        while (tableMatcher.find()) {
            String tableName = tableMatcher.group(2);
            int start = tableMatcher.start(2);
            CodeNode tableNode = new CodeNode(NodeType.TABLE, tableName, baseOffset + start);
            tableNode.setEndOffset(baseOffset + start + tableName.length());
            stmtNode.addChild(tableNode);
        }
        
        Matcher procMatcher = PROC_PATTERN.matcher(text);
        while (procMatcher.find()) {
            String procName = procMatcher.group(2);
            int start = procMatcher.start(2);
            // Use PROCEDURE type for both procedures and functions for consistency with original code
            CodeNode procNode = new CodeNode(NodeType.PROCEDURE, procName, baseOffset + start);
            procNode.setEndOffset(baseOffset + start + procName.length());
            stmtNode.addChild(procNode);
        }
    }

    private int findStatementEnd(String source, int start) {
        int pos = start;
        boolean inString = false;
        char quoteChar = 0;
        
        while (pos < source.length()) {
            char c = source.charAt(pos);
            if (inString) {
                if (c == quoteChar) {
                    if (pos + 1 < source.length() && source.charAt(pos + 1) == quoteChar) {
                        pos++; // escaped quote
                    } else {
                        inString = false;
                    }
                }
            } else {
                if (c == '\'' || c == '"') {
                    inString = true;
                    quoteChar = c;
                } else if (c == ';') {
                    return pos;
                } else if (c == '-' && pos + 1 < source.length() && source.charAt(pos + 1) == '-') {
                    return pos;
                } else if (c == '/' && pos + 1 < source.length() && source.charAt(pos + 1) == '*') {
                    return pos;
                }
            }
            pos++;
        }
        return pos;
    }
}
