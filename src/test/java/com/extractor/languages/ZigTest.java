package com.extractor.languages;

import com.extractor.model.CodeNode;
import com.extractor.model.NodeType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ZigTest extends LanguageTestBase {

    @Test
    public void testValidZig() {
        String content = readResource("zig/valid.zig");
        CodeNode root = extract("valid.zig", content);
        
        assertNotNull(root);
        assertNodeExists(root, NodeType.FUNCTION, "main");
        assertNodeExists(root, NodeType.FUNCTION, "add");
    }

    @Test
    public void testInvalidZig() {
        String content = readResource("zig/invalid.zig");
        CodeNode root = extract("invalid.zig", content);
        
        assertNotNull(root);
        assertNodeExists(root, NodeType.FUNCTION, "main");
    }
}