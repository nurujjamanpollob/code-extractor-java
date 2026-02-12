package com.extractor.languages;

import com.extractor.model.CodeNode;
import com.extractor.model.NodeType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CSSTest extends LanguageTestBase {

    @Test
    public void testValidCSS() {
        String content = readResource("css/valid.css");
        CodeNode root = extract("valid.css", content);
        
        assertNotNull(root);
        assertNodeExists(root, NodeType.SELECTOR, "body");
        assertNodeExists(root, NodeType.SELECTOR, ".container");
        assertNodeExists(root, NodeType.SELECTOR, "#header");
        assertNodeExists(root, NodeType.SELECTOR, ".btn:hover");
    }

    @Test
    public void testInvalidCSS() {
        String content = readResource("css/invalid.css");
        CodeNode root = extract("invalid.css", content);
        
        assertNotNull(root);
        assertNodeExists(root, NodeType.SELECTOR, "body");
        assertNodeExists(root, NodeType.SELECTOR, ".unclosed");
        assertNodeExists(root, NodeType.SELECTOR, ".next-rule");
    }
}