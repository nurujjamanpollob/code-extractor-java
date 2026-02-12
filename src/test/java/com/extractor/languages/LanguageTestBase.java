package com.extractor.languages;

import com.extractor.ExtractionEngine;
import com.extractor.model.CodeNode;
import com.extractor.model.NodeType;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public abstract class LanguageTestBase {
    protected final ExtractionEngine engine = new ExtractionEngine();

    protected String readResource(String path) {
        try {
            return Files.readString(Paths.get("I:\\code-extractor-java\\src\\test\\resources\\languages", path));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read resource: " + path, e);
        }
    }

    protected CodeNode extract(String fileName, String content) {
        return engine.extract(fileName, content);
    }

    protected void assertNodeExists(CodeNode root, NodeType type, String name) {
        List<CodeNode> nodes = root.findByType(type);
        assertTrue(nodes.stream().anyMatch(n -> n.getName().equals(name)), 
            "Expected node of type " + type + " with name '" + name + "' not found");
    }

    protected void assertNodeTypeCount(CodeNode root, NodeType type, int expectedCount) {
        List<CodeNode> nodes = root.findByType(type);
        assertEquals(expectedCount, nodes.size(), 
            "Expected " + expectedCount + " nodes of type " + type + " but found " + nodes.size());
    }
}