package com.extractor.model;

public enum NodeType {
    ROOT,
    CLASS,
    INTERFACE,
    METHOD,
    CONSTRUCTOR,
    VARIABLE,
    FIELD,
    TAG,         // HTML/XML
    SELECTOR,    // CSS
    PROPERTY,    // CSS
    FUNCTION,
    BLOCK,       // Generic code block
    IMPORT,
    NAMESPACE,
    DECORATOR,
    ANNOTATION,
    STRUCT,      // C/Go/Rust
    ENUM,
    TRAIT,       // Rust/PHP/Scala
    MODULE,      // Python/Ruby/Go
    QUERY,       // SQL
    TABLE,       // SQL
    PROCEDURE,   // SQL
    STATEMENT,   // Generic statement (e.g., SQL, Shell)
    OBJECT,      // JS/JSON
    COMMENT,     // Single-line or multi-line comment
    UNKNOWN
}
