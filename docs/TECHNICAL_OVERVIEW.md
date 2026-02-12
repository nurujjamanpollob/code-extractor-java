# Technical Overview

This document describes the internal mechanics of the `code-extractor-java` library and how it processes source code into a structured tree.

## The Extraction Pipeline

The extraction process follows a linear pipeline managed by the `ExtractionEngine`:

1.  **Input**: The engine receives a file name and its string content.
2.  **Language Detection**: The `LanguageDetector` maps the file extension to a `CodeParser` (e.g., `.java` -> `JavaParser`).
3.  **Source Scanning**: The content is loaded into a `SourceScanner`, which provides character-by-character navigation and lookahead capabilities.
4.  **Lexical Analysis & Parsing**: The selected parser iterates through the source code:
    *   **Literal Skipping**: Automatically skips content inside string literals and characters to prevent syntax misinterpretation.
    *   **Comment Extraction**: Identifies and captures comments as individual nodes.
    *   **Scope Tracking**: Uses a stack-based approach to manage nested structures (e.g., curly braces or indentation).
    *   **Rule Matching**: Applies language-specific regex rules to identify declaration headers (like `public class MyClass {`).
5.  **Tree Construction**: As structures are identified, `CodeNode` objects are created and organized into a hierarchical tree.
6.  **Output**: The engine returns the `ROOT` node of the tree.

## Core Components

### Extraction Engine (`ExtractionEngine`)
The central orchestrator. It maintains a registry of available parsers and delegates the work based on file types.

### Base Parser (`BaseParser`)
An abstract class providing shared logic for:
- Character-based scanning.
- Handling of string and character literals.
- Common comment formats (C-style, Python-style, etc.).
- Generic block identification.

### Code Node (`CodeNode`)
The data model representing a structural element. Each node contains:
- **Type**: (e.g., `CLASS`, `METHOD`, `BLOCK`).
- **Name**: The identifier of the element.
- **Content**: The full source code belonging to that node.
- **Offsets**: Start and end positions in the original file.
- **Children**: A list of nested `CodeNode` objects.

## Parsing Strategy: Resilient & Rule-Based

Unlike full compilers that require perfectly valid code and complex ASTs, `code-extractor-java` uses a **resilient parsing strategy**:

- **Tolerance**: It can handle partially finished or syntactically incorrect code.
- **Heuristics**: It uses patterns and keywords to "guess" the structure when exact syntax is ambiguous.
- **Fallback**: If a block of code (e.g., between `{}`) cannot be identified as a class or method, it is preserved as a generic `BLOCK` node, ensuring no code is lost in the model.

## Indentation-Based Parsing (Python/GDScript)
For languages where whitespace is significant, the library employs a specialized `IndentationScanner`. This tracker monitors the leading whitespace of each line to determine the start and end of code blocks, rather than relying on explicit delimiters like `{}`.
