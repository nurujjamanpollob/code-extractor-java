package com.extractor.languages;

import com.extractor.model.CodeNode;
import com.extractor.model.NodeType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RustTest extends LanguageTestBase {

    @Test
    public void testValidRust() {
        String content = readResource("rust/valid.rs");
        CodeNode root = extract("valid.rs", content);
        
        assertNotNull(root);
        assertNodeExists(root, NodeType.STRUCT, "Person");
        assertNodeExists(root, NodeType.FUNCTION, "new");
        assertNodeExists(root, NodeType.FUNCTION, "greet");
        assertNodeExists(root, NodeType.FUNCTION, "main");
    }

    @Test
    public void testInvalidRust() {
        String content = readResource("rust/invalid.rs");
        CodeNode root = extract("invalid.rs", content);
        
        assertNotNull(root);
        assertNodeExists(root, NodeType.FUNCTION, "broken");
    }
}