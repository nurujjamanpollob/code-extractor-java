package com.extractor.query;

import com.extractor.model.CodeNode;
import com.extractor.model.NodeType;

import java.util.List;
import java.util.stream.Collectors;

public class CodeQuery {
    
    public static List<CodeNode> findMethodsByName(CodeNode root, String name) {
        return root.findByType(NodeType.METHOD).stream()
                .filter(n -> n.getName().contains(name))
                .collect(Collectors.toList());
    }

    public static List<CodeNode> findClassesWithMethods(CodeNode root) {
        return root.getClasses().stream()
                .filter(c -> !c.getMethods().isEmpty())
                .collect(Collectors.toList());
    }

    public static List<CodeNode> getAllDefinitions(CodeNode root) {
        return root.getChildren().stream()
                .filter(n -> n.getType() != NodeType.BLOCK && n.getType() != NodeType.ROOT)
                .collect(Collectors.toList());
    }
}
