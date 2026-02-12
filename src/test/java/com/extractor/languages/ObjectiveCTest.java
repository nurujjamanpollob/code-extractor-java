package com.extractor.languages;

import com.extractor.model.CodeNode;
import com.extractor.model.NodeType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ObjectiveCTest extends LanguageTestBase {

    @Test
    public void testValidObjectiveC() {
        String content = readResource("objectivec/valid.m");
        CodeNode root = extract("valid.m", content);
        
        assertNotNull(root);
        // Objective-C uses @implementation for classes
        assertNodeExists(root, NodeType.CLASS, "Person");
        assertNodeExists(root, NodeType.METHOD, "sayHello");
    }
}