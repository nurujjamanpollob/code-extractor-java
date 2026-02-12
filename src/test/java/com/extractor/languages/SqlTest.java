package com.extractor.languages;

import com.extractor.model.CodeNode;
import com.extractor.model.NodeType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SqlTest extends LanguageTestBase {

    @Test
    @DisplayName("Test comprehensive SQL extraction covering DDL, DML, DCL, and TCL")
    void testComprehensiveSqlExtraction() {
        String content = readResource("sql/comprehensive.sql");
        CodeNode root = extract("comprehensive.sql", content);

        assertNotNull(root);
        assertEquals(NodeType.ROOT, root.getType());

        // DDL Assertions
        assertNodeExists(root, NodeType.STATEMENT, "CREATE");
        assertNodeExists(root, NodeType.TABLE, "employees");
        assertNodeExists(root, NodeType.STATEMENT, "ALTER");
        assertNodeExists(root, NodeType.TABLE, "employees");
        assertNodeExists(root, NodeType.STATEMENT, "DROP");
        assertNodeExists(root, NodeType.TABLE, "temp_data");

        // DML Assertions
        assertNodeExists(root, NodeType.STATEMENT, "INSERT");
        assertNodeExists(root, NodeType.STATEMENT, "UPDATE");
        assertNodeExists(root, NodeType.STATEMENT, "DELETE");
        assertNodeExists(root, NodeType.STATEMENT, "SELECT");

        // CTE and Window Function check (should be part of a SELECT statement)
        List<CodeNode> selectNodes = root.findByType(NodeType.STATEMENT).stream()
                .filter(n -> n.getName().equals("SELECT"))
                .toList();
        assertTrue(selectNodes.size() >= 2, "Expected at least 2 SELECT statements");
        
        boolean foundCte = selectNodes.stream().anyMatch(n -> n.getContent().contains("WITH DeptAvg"));
        assertTrue(foundCte, "CTE statement not found in SELECT nodes");

        // DCL Assertions
        assertNodeExists(root, NodeType.STATEMENT, "GRANT");
        assertNodeExists(root, NodeType.STATEMENT, "REVOKE");

        // TCL Assertions
        assertNodeExists(root, NodeType.STATEMENT, "BEGIN");
        assertNodeExists(root, NodeType.STATEMENT, "COMMIT");
        assertNodeExists(root, NodeType.STATEMENT, "ROLLBACK");

        // Stored Procedure and Function
        // Note: SqlParser identifies both as PROCEDURE type
        assertNodeExists(root, NodeType.PROCEDURE, "GetEmployeeById");
        assertNodeExists(root, NodeType.PROCEDURE, "CalculateBonus");

        // Comments
        List<CodeNode> comments = root.findByType(NodeType.COMMENT);
        assertTrue(comments.size() >= 3, "Expected at least 3 comment blocks");
        
        // Check for specific comment content
        boolean foundMultiLine = comments.stream().anyMatch(c -> c.getContent().contains("Multi-line comment"));
        assertTrue(foundMultiLine, "Multi-line comment not extracted correctly");
    }

    @Test
    @DisplayName("Test SQL extraction with escaped characters in strings")
    void testEscapedCharacters() {
        String content = "SELECT 'It''s a test' FROM dual; SELECT \"Double \"\"quote\"\" test\" FROM dual;";
        CodeNode root = extract("escaped.sql", content);

        List<CodeNode> selectNodes = root.findByType(NodeType.STATEMENT);
        assertEquals(2, selectNodes.size());
        assertTrue(selectNodes.get(0).getContent().contains("'It''s a test'"));
        assertTrue(selectNodes.get(1).getContent().contains("\"Double \"\"quote\"\" test\""));
    }

    @Test
    @DisplayName("Test SQL extraction with malformed input")
    void testMalformedSql() {
        String content = readResource("sql/malformed.sql");
        CodeNode root = extract("malformed.sql", content);

        assertNotNull(root);
        // Even with malformed SQL, the parser should extract whatever it recognizes
        List<CodeNode> statements = root.findByType(NodeType.STATEMENT);
        assertFalse(statements.isEmpty(), "Should have extracted at least some valid statements from malformed file");
        
        // It should still find the keywords it recognizes
        assertNodeExists(root, NodeType.STATEMENT, "INSERT");
        assertNodeExists(root, NodeType.STATEMENT, "SELECT");
        assertNodeExists(root, NodeType.STATEMENT, "CREATE");
    }

    @Test
    @DisplayName("Test SQL extraction with large file performance")
    void testLargeSqlPerformance() {
        String content = readResource("sql/large.sql");
        
        long startTime = System.currentTimeMillis();
        CodeNode root = extract("large.sql", content);
        long duration = System.currentTimeMillis() - startTime;

        assertNotNull(root);
        // Performance assertion: should be very fast
        assertTrue(duration < 2000, "Parsing took too long: " + duration + "ms");
        
        List<CodeNode> statements = root.findByType(NodeType.STATEMENT);
        assertTrue(statements.size() >= 11, "Should have extracted all statements from large file");
    }

    @Test
    @DisplayName("Test SQL extraction with empty or null input")
    void testEmptyInput() {
        CodeNode root = extract("empty.sql", "");
        assertNotNull(root);
        assertEquals(0, root.getChildren().size());
        
        root = extract("null.sql", null);
        assertNotNull(root);
        assertEquals(0, root.getChildren().size());
    }
}
