package com.extractor.languages;

import com.extractor.model.CodeNode;
import com.extractor.model.NodeType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ElixirTest extends LanguageTestBase {

    @Test
    public void testValidElixir() {
        String content = readResource("elixir/valid.ex");
        CodeNode root = extract("valid.ex", content);
        
        assertNotNull(root);
        assertNodeExists(root, NodeType.MODULE, "MyApp.User");
        assertNodeExists(root, NodeType.MODULE, "MyApp.Main");
        assertNodeExists(root, NodeType.FUNCTION, "new");
        assertNodeExists(root, NodeType.FUNCTION, "greet");
        assertNodeExists(root, NodeType.FUNCTION, "run");
    }
}