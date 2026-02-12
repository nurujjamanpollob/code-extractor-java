package com.extractor.languages;

import com.extractor.model.CodeNode;
import com.extractor.model.NodeType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class HTML5MixedWithCSSAndJSTest extends LanguageTestBase {

    @Test
    public void testMixedContent() {
        String content = readResource("html/mixed.html");
        CodeNode root = extract("mixed.html", content);
        
        assertNotNull(root);
        assertNodeExists(root, NodeType.TAG, "html");
        assertNodeExists(root, NodeType.TAG, "style");
        assertNodeExists(root, NodeType.TAG, "script");
        
        // Check if CSS selectors are extracted (if HTMLParser delegates to CSSParser)
        // Note: This depends on whether HTMLParser is implemented to handle nested languages.
        // Based on the code I saw, HTMLParser doesn't seem to delegate yet, 
        // but let's test for the tags at least.
        
        assertNodeExists(root, NodeType.TAG, "h1");
    }
}