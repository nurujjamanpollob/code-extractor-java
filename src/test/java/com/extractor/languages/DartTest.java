package com.extractor.languages;

import com.extractor.model.CodeNode;
import com.extractor.model.NodeType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DartTest extends LanguageTestBase {

    @Test
    public void testValidDart() {
        String content = readResource("dart/valid.dart");
        CodeNode root = extract("valid.dart", content);
        
        assertNotNull(root);
        assertNodeExists(root, NodeType.CLASS, "MyApp");
        assertNodeExists(root, NodeType.CLASS, "User");
        assertNodeExists(root, NodeType.FUNCTION, "main");
    }

    @Test
    public void testInvalidDart() {
        String content = readResource("dart/invalid.dart");
        CodeNode root = extract("invalid.dart", content);
        
        assertNotNull(root);
        assertNodeExists(root, NodeType.FUNCTION, "main");
        assertNodeExists(root, NodeType.CLASS, "Broken");
    }
}