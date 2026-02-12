package com.extractor.languages;

import com.extractor.model.CodeNode;
import com.extractor.model.NodeType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class JavaTest extends LanguageTestBase {

    @Test
    public void testValidJava() {
        String content = readResource("java/valid.java");
        CodeNode root = extract("Sample.java", content);
        
        assertNotNull(root);
        assertNodeExists(root, NodeType.CLASS, "Sample");
        assertNodeExists(root, NodeType.METHOD, "process");
        assertNodeExists(root, NodeType.METHOD, "main");
    }

    @Test
    public void testComprehensiveJava() {
        String content = readResource("java/comprehensive.java");
        CodeNode root = extract("Comprehensive.java", content);
        
        assertNotNull(root);
        assertNodeExists(root, NodeType.CLASS, "Comprehensive");
        assertNodeExists(root, NodeType.METHOD, "toString");
        assertNodeExists(root, NodeType.METHOD, "processItems");
        assertNodeExists(root, NodeType.CLASS, "InnerClass");
        assertNodeExists(root, NodeType.METHOD, "innerMethod");
        
        // Check for comments
        long commentCount = root.descendants().stream()
                .filter(n -> n.getType() == NodeType.COMMENT)
                .count();
        assertTrue(commentCount >= 4, "Should find at least 4 comments");
        
        // Check for anonymous blocks (like static block or lambda body)
        long blockCount = root.descendants().stream()
                .filter(n -> n.getType() == NodeType.BLOCK)
                .count();
        assertTrue(blockCount >= 2, "Should find at least 2 anonymous blocks (static block, lambda, etc.)");
    }

    @Test
    public void testMalformedJava() {
        String content = readResource("java/malformed.java");
        CodeNode root = extract("Malformed.java", content);
        
        assertNotNull(root);
        // Even if malformed, it should at least identify the class
        assertNodeExists(root, NodeType.CLASS, "Malformed");
        
        // It should handle the unclosed structures without crashing
        assertTrue(root.getEndOffset() > 0);
    }

    @Test
    public void testInvalidJava() {
        String content = readResource("java/invalid.java");
        CodeNode root = extract("Broken.java", content);
        
        assertNotNull(root);
        assertNodeExists(root, NodeType.CLASS, "Broken");
    }
}
