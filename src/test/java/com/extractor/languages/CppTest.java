package com.extractor.languages;

import com.extractor.model.CodeNode;
import com.extractor.model.NodeType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CppTest extends LanguageTestBase {

    @Test
    public void testValidCpp() {
        String content = readResource("cpp/valid.cpp");
        CodeNode root = extract("sample.cpp", content);
        
        assertNotNull(root);
        // Updated to match the actual class names in valid.cpp
        assertNodeExists(root, NodeType.CLASS, "Shape");
        assertNodeExists(root, NodeType.CLASS, "Circle");
        assertNodeExists(root, NodeType.FUNCTION, "main");
    }

    @Test
    public void testComprehensiveCpp() {
        String content = readResource("cpp/comprehensive.cpp");
        CodeNode root = extract("comprehensive.cpp", content);
        
        assertNotNull(root);
        assertNodeExists(root, NodeType.NAMESPACE, "extractor");
        assertNodeExists(root, NodeType.CLASS, "Comprehensive");
        assertNodeExists(root, NodeType.METHOD, "process");
        assertNodeExists(root, NodeType.FUNCTION, "main");
    }

    @Test
    public void testMalformedCpp() {
        String content = readResource("cpp/malformed.cpp");
        CodeNode root = extract("malformed.cpp", content);
        
        assertNotNull(root);
        assertNodeExists(root, NodeType.CLASS, "Malformed");
    }
}
