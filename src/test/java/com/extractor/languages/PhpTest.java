package com.extractor.languages;

import com.extractor.model.CodeNode;
import com.extractor.model.NodeType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for PHP code extraction.
 */
public class PhpTest extends LanguageTestBase {

    @Test
    public void testComprehensiveExtraction() {
        String content = readResource("php/comprehensive.php");
        CodeNode root = extract("comprehensive.php", content);

        assertNotNull(root);
        assertEquals(NodeType.ROOT, root.getType());

        // Verify Interface
        assertNodeExists(root, NodeType.INTERFACE, "LoggerInterface");
        
        // Verify Trait
        assertNodeExists(root, NodeType.TRAIT, "TimestampTrait");

        // Verify Class
        assertNodeExists(root, NodeType.CLASS, "BaseService");

        // Verify Methods inside class
        CodeNode baseService = root.findByType(NodeType.CLASS).stream()
                .filter(n -> "BaseService".equals(n.getName()))
                .findFirst()
                .orElseThrow();
        
        assertNodeExists(baseService, NodeType.METHOD, "__construct");
        assertNodeExists(baseService, NodeType.METHOD, "log");

        // Verify Standalone Function
        assertNodeExists(root, NodeType.METHOD, "helper_function");

        // Verify Anonymous Block
        assertNodeExists(root, NodeType.BLOCK, "anonymous");

        // Verify Comments
        // 1. DocBlock for LoggerInterface
        // 2. Shell comment # Shell-style comment
        // 3. Line comment // Single-line comment
        // 4. Block comment /* Standalone function */
        // 5. Line comment // Anonymous block/scope test
        assertNodeTypeCount(root, NodeType.COMMENT, 5);
    }

    @Test
    public void testValidPhpExtraction() {
        String content = readResource("php/valid.php");
        CodeNode root = extract("valid.php", content);

        assertNotNull(root);
        assertNodeExists(root, NodeType.CLASS, "UserController");
        
        CodeNode controller = root.findByType(NodeType.CLASS).get(0);
        assertNodeExists(controller, NodeType.METHOD, "index");
        assertNodeExists(controller, NodeType.METHOD, "validate");
    }

    @Test
    public void testInvalidPhpExtraction() {
        String content = readResource("php/invalid.php");
        CodeNode root = extract("invalid.php", content);

        assertNotNull(root);
        // The parser should still find the function even if it's missing a semicolon inside
        assertNodeExists(root, NodeType.METHOD, "missingSemicolon");
        
        // The Incomplete class is missing a closing brace, 
        // but it should still be identified as a class.
        assertNodeExists(root, NodeType.CLASS, "Incomplete");
    }

    @Test
    public void testPhpCommentStyles() {
        String content = "<?php\n" +
                "// Line comment\n" +
                "# Shell comment\n" +
                "/* Block comment */\n" +
                "/** Doc comment */\n" +
                "class Test {}\n" +
                "?>";
        CodeNode root = extract("comments.php", content);

        // PHPParser handles // and /* */ and #
        assertNodeTypeCount(root, NodeType.COMMENT, 4);
    }
    
    @Test
    public void testNestedStructures() {
        String content = "<?php\n" +
                "class Outer {\n" +
                "    public function outerMethod() {\n" +
                "        if (true) {\n" +
                "            // inside if\n" +
                "        }\n" +
                "    }\n" +
                "}\n" +
                "?>";
        CodeNode root = extract("nested.php", content);
        
        CodeNode outer = root.findByType(NodeType.CLASS).get(0);
        CodeNode method = outer.getChildren().stream()
                .filter(n -> n.getType() == NodeType.METHOD)
                .findFirst().orElseThrow();
        
        // The 'if (true) {' should create an anonymous block inside the method
        assertNodeExists(method, NodeType.BLOCK, "anonymous");
    }
}
