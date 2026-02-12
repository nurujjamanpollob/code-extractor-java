package com.extractor.languages;

import com.extractor.model.CodeNode;
import com.extractor.model.NodeType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ShellTest extends LanguageTestBase {

    @Test
    public void testValidShell() {
        String content = readResource("shell/valid.sh");
        CodeNode root = extract("valid.sh", content);
        
        assertNotNull(root);
        assertNodeExists(root, NodeType.FUNCTION, "greet");
        assertNodeExists(root, NodeType.VARIABLE, "NAME");
    }

    @Test
    public void testInvalidShell() {
        String content = readResource("shell/invalid.sh");
        CodeNode root = extract("invalid.sh", content);
        
        assertNotNull(root);
        // Shell parser should still find functions even if syntax is slightly off
        assertNodeExists(root, NodeType.FUNCTION, "broken");
    }
}