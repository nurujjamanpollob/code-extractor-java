package com.extractor.languages;

import com.extractor.model.CodeNode;
import com.extractor.model.NodeType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ScalaTest extends LanguageTestBase {

    @Test
    public void testValidScala() {
        String content = readResource("scala/valid.scala");
        CodeNode root = extract("valid.scala", content);
        
        assertNotNull(root);
        assertNodeExists(root, NodeType.CLASS, "User");
        assertNodeExists(root, NodeType.CLASS, "Processor");
        assertNodeExists(root, NodeType.METHOD, "greet");
        assertNodeExists(root, NodeType.METHOD, "process");
    }
}