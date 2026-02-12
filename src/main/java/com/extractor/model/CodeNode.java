package com.extractor.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CodeNode {
    private NodeType type;
    private String name;
    private int startOffset;
    private int endOffset;
    private String content;
    private List<CodeNode> children = new ArrayList<>();
    private Map<String, Object> metadata = new HashMap<>();

    public CodeNode(NodeType type, String name, int startOffset) {
        this.type = type;
        this.name = name;
        this.startOffset = startOffset;
        this.endOffset = startOffset;
    }

    public CodeNode(NodeType type, String name, int startOffset, int endOffset) {
        this.type = type;
        this.name = name;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
    }

    public void addChild(CodeNode child) {
        if (child != null) {
            children.add(child);
        }
    }

    // Getters and Setters
    public NodeType getType() { return type; }
    public void setType(NodeType type) { this.type = type; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getStartOffset() { return startOffset; }
    public void setStartOffset(int startOffset) { this.startOffset = startOffset; }

    public int getEndOffset() { return endOffset; }
    public void setEndOffset(int endOffset) { this.endOffset = endOffset; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public List<CodeNode> getChildren() { return children; }

    public void addMetadata(String key, Object value) {
        metadata.put(key, value);
    }

    public List<CodeNode> findByType(NodeType type) {
        List<CodeNode> result = new ArrayList<>();
        if (this.type == type) {
            result.add(this);
        }
        for (CodeNode child : children) {
            result.addAll(child.findByType(type));
        }
        return result;
    }

    /**
     * Returns a list of all descendant nodes (children, grandchildren, etc.)
     * in a depth-first order.
     * @return List of all descendant CodeNodes
     */
    public List<CodeNode> descendants() {
        List<CodeNode> result = new ArrayList<>();
        for (CodeNode child : children) {
            result.add(child);
            result.addAll(child.descendants());
        }
        return result;
    }

    public List<CodeNode> getClasses() {
        return findByType(NodeType.CLASS);
    }

    public List<CodeNode> getMethods() {
        return findByType(NodeType.METHOD);
    }

    public List<CodeNode> getFunctions() {
        return findByType(NodeType.FUNCTION);
    }

    public List<CodeNode> getComments() {
        return findByType(NodeType.COMMENT);
    }

    public Map<String, Object> toMcpContext() {
        Map<String, Object> map = new HashMap<>();
        map.put("type", type.toString());
        map.put("name", name);
        map.put("start", startOffset);
        map.put("end", endOffset);
        
        if (content != null && !content.isEmpty()) {
            map.put("content", content);
        }

        if (!children.isEmpty()) {
            map.put("children", children.stream()
                    .map(CodeNode::toMcpContext)
                    .collect(Collectors.toList()));
        }

        if (!metadata.isEmpty()) {
            map.put("metadata", metadata);
        }

        return map;
    }

    @Override
    public String toString() {
        return String.format("%s: %s (%d-%d)", type, name, startOffset, endOffset);
    }
}
