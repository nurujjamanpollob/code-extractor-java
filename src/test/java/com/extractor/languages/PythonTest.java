package com.extractor.languages;

import com.extractor.model.CodeNode;
import com.extractor.model.NodeType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PythonTest extends LanguageTestBase {

    @Test
    public void testValidPython() {
        String content = readResource("python/valid.py");
        CodeNode root = extract("sample.py", content);
        
        assertNotNull(root);
        assertNodeExists(root, NodeType.CLASS, "Sample");
        assertNodeExists(root, NodeType.FUNCTION, "process");
        assertNodeExists(root, NodeType.FUNCTION, "main");
    }

    @Test
    public void testComprehensivePython() {
        String content = readResource("python/comprehensive.py");
        CodeNode root = extract("comprehensive.py", content);
        
        assertNotNull(root);
        assertNodeExists(root, NodeType.CLASS, "Comprehensive");
        assertNodeExists(root, NodeType.FUNCTION, "process_data");
        assertNodeExists(root, NodeType.FUNCTION, "top_level_function");
        
        // Check for nested functions
        assertNodeExists(root, NodeType.FUNCTION, "nested_helper");
    }

    @Test
    public void testMalformedPython() {
        String content = readResource("python/malformed.py");
        CodeNode root = extract("malformed.py", content);
        
        assertNotNull(root);
        // It should still find the class despite indentation errors in its methods
        assertNodeExists(root, NodeType.CLASS, "Malformed");
    }
}
