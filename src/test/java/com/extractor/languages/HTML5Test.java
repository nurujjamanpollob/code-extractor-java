package com.extractor.languages;

import com.extractor.model.CodeNode;
import com.extractor.model.NodeType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class HTML5Test extends LanguageTestBase {

    @Test
    public void testValidHTML5() {
        String content = readResource("html/valid.html");
        CodeNode root = extract("valid.html", content);
        
        assertNotNull(root);
        assertNodeExists(root, NodeType.TAG, "html");
        assertNodeExists(root, NodeType.TAG, "head");
        assertNodeExists(root, NodeType.TAG, "body");
        assertNodeExists(root, NodeType.TAG, "header");
        assertNodeExists(root, NodeType.TAG, "main");
        assertNodeExists(root, NodeType.TAG, "footer");
        assertNodeExists(root, NodeType.TAG, "h1");
        assertNodeExists(root, NodeType.TAG, "p");
        assertNodeExists(root, NodeType.TAG, "img");
    }

    @Test
    public void testInvalidHTML5() {
        String content = readResource("html/invalid.html");
        CodeNode root = extract("invalid.html", content);
        
        assertNotNull(root);
        // Even with unclosed tags, the parser should extract what it can
        assertNodeExists(root, NodeType.TAG, "h1");
        assertNodeExists(root, NodeType.TAG, "p");
        assertNodeExists(root, NodeType.TAG, "div");
        assertNodeExists(root, NodeType.TAG, "span");
    }
}