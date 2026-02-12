package com.extractor.languages;

import com.extractor.model.CodeNode;
import com.extractor.model.NodeType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class JavaScriptTest extends LanguageTestBase {

    @Test
    public void testValidJavaScript() {
        String content = readResource("javascript/valid.js");
        CodeNode root = extract("sample.js", content);

        assertNotNull(root);
        assertNodeExists(root, NodeType.CLASS, "Sample");
        assertNodeExists(root, NodeType.METHOD, "process");
    }

    @Test
    public void testComprehensiveJavaScript() {
        String content = readResource("javascript/comprehensive.js");
        CodeNode root = extract("comprehensive.js", content);

        assertNotNull(root);
        assertNodeExists(root, NodeType.CLASS, "Comprehensive");

        // Verify async method is extracted (covers async/await handling)
        assertNodeExists(root, NodeType.METHOD, "processData");

        // Verify static method defined after the async method is detected
        // This ensures the parser didn't break on the async method structure
        assertNodeExists(root, NodeType.METHOD, "create");

        assertNodeExists(root, NodeType.FUNCTION, "topLevelFunction");
    }

    @Test
    public void testMalformedJavaScript() {
        String content = readResource("javascript/malformed.js");
        CodeNode root = extract("malformed.js", content);

        assertNotNull(root);
        assertNodeExists(root, NodeType.CLASS, "Malformed");
    }
}
