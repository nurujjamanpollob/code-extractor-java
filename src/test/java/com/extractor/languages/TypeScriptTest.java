package com.extractor.languages;

import com.extractor.model.CodeNode;
import com.extractor.model.NodeType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TypeScriptTest extends LanguageTestBase {

    @Test
    public void testValidTS() {
        String content = readResource("typescript/valid.ts");
        CodeNode root = extract("valid.ts", content);
        
        assertNotNull(root);
        assertNodeExists(root, NodeType.INTERFACE, "User");
        assertNodeExists(root, NodeType.CLASS, "Account");
        assertNodeExists(root, NodeType.METHOD, "addUser");
        assertNodeExists(root, NodeType.VARIABLE, "admin");
    }

    @Test
    public void testInvalidTS() {
        String content = readResource("typescript/invalid.ts");
        CodeNode root = extract("invalid.ts", content);
        
        assertNotNull(root);
        assertNodeExists(root, NodeType.INTERFACE, "Broken");
        assertNodeExists(root, NodeType.FUNCTION, "oops");
    }
}