package com.extractor.languages;

import com.extractor.model.CodeNode;
import com.extractor.model.NodeType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class KotlinTest extends LanguageTestBase {

    @Test
    public void testValidKotlin() {
        String content = readResource("kotlin/valid.kt");
        CodeNode root = extract("valid.kt", content);
        
        assertNotNull(root);
        assertNodeExists(root, NodeType.CLASS, "User");
        assertNodeExists(root, NodeType.INTERFACE, "Repository");
        assertNodeExists(root, NodeType.METHOD, "addUser");
        assertNodeExists(root, NodeType.FUNCTION, "main");
    }

    @Test
    public void testInvalidKotlin() {
        String content = readResource("kotlin/invalid.kt");
        CodeNode root = extract("invalid.kt", content);
        
        assertNotNull(root);
        assertNodeExists(root, NodeType.FUNCTION, "broken");
    }
}
