package com.extractor.languages;

import com.extractor.model.CodeNode;
import com.extractor.model.NodeType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class GDScriptTest extends LanguageTestBase {

    @Test
    public void testValidGDScript() {
        String content = readResource("gdscript/valid.gd");
        CodeNode root = extract("valid.gd", content);
        
        assertNotNull(root);
        assertNodeExists(root, NodeType.FUNCTION, "_ready");
        assertNodeExists(root, NodeType.FUNCTION, "_process");
        assertNodeExists(root, NodeType.FUNCTION, "take_damage");
        assertNodeExists(root, NodeType.VARIABLE, "speed");
    }

    @Test
    public void testInvalidGDScript() {
        String content = readResource("gdscript/invalid.gd");
        CodeNode root = extract("invalid.gd", content);
        
        assertNotNull(root);
        assertNodeExists(root, NodeType.FUNCTION, "_ready");
    }
}