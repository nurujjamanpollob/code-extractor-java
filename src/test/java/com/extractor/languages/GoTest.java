package com.extractor.languages;

import com.extractor.model.CodeNode;
import com.extractor.model.NodeType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class GoTest extends LanguageTestBase {

    @Test
    public void testValidGo() {
        String content = readResource("go/valid.go");
        CodeNode root = extract("sample.go", content);
        
        assertNotNull(root);
        assertNodeExists(root, NodeType.STRUCT, "Sample");
    }

    @Test
    public void testComprehensiveGo() {
        String content = readResource("go/comprehensive.go");
        CodeNode root = extract("comprehensive.go", content);
        
        assertNotNull(root);
        assertNodeExists(root, NodeType.STRUCT, "Comprehensive");
        assertNodeExists(root, NodeType.METHOD, "Process");
        assertNodeExists(root, NodeType.INTERFACE, "Processor");
        assertNodeExists(root, NodeType.FUNCTION, "Compute");
    }

    @Test
    public void testMalformedGo() {
        String content = readResource("go/malformed.go");
        CodeNode root = extract("malformed.go", content);
        
        assertNotNull(root);
        // It should still find the package at least
        assertTrue(root.getChildren().size() > 0);
    }
}
