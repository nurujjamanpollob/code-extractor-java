package com.extractor.languages;

import com.extractor.model.CodeNode;
import com.extractor.model.NodeType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SwiftTest extends LanguageTestBase {

    @Test
    public void testValidSwift() {
        String content = readResource("swift/valid.swift");
        CodeNode root = extract("valid.swift", content);
        
        assertNotNull(root);
        assertNodeExists(root, NodeType.STRUCT, "User");
        assertNodeExists(root, NodeType.CLASS, "UserManager");
        assertNodeExists(root, NodeType.METHOD, "addUser");
        assertNodeExists(root, NodeType.METHOD, "findUser");
    }
}
