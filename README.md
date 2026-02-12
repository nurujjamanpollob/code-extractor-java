# Code Extractor (Java)

A powerful, multi-language code structure extraction library built in Java.

## Overview

`code-extractor-java` is a library designed to scan source code files and extract hierarchical structures such as classes, methods, functions, and comments. It provides a unified model (`CodeNode`) for representing code regardless of the source language, making it ideal for tools like:

- **AI Assistants**: Providing structured context about codebases.
- **Code Analyzers**: Building search indexes or structural maps.
- **Documentation Generators**: Quickly identifying key components in a project.

## Key Features

- **Multi-Language Support**: Support for 20+ languages (Java, Python, C++, Go, Rust, etc.).
- **Resilient Parsing**: Handles incomplete or syntactically incorrect code.
- **No Heavy Dependencies**: Built with core Java logic and minimal external libraries.
- **MCP Compatibility**: Built-in support for exporting to Model Context Protocol format.
- **Indentation & Brace Support**: Specialized scanners for both brace-delimited and indentation-delimited languages.

## Quick Start

```java
ExtractionEngine engine = new ExtractionEngine();
CodeNode root = engine.extract("Main.java", sourceCodeString);

// Print all method names
root.getMethods().forEach(m -> System.out.println(m.getName()));
```

## Documentation

- [**Installation Guide**](docs/INSTALLATION.md) - How to setup and build the project.
- [**Usage Examples**](docs/USAGE.md) - Code snippets for common tasks.
- [**Supported Languages**](docs/LANGUAGES.md) - Detailed list of supported languages and features.
- [**Technical Overview**](docs/TECHNICAL_OVERVIEW.md) - How the extraction engine works.
- [**Configuration & Formats**](docs/CONFIGURATION.md) - Customizing output and formats.
- [**Contribution Guidelines**](docs/CONTRIBUTING.md) - How to help improve the project.

## Supported Languages (Highlights)

| | | | |
| :--- | :--- | :--- | :--- |
| Java | Python | C / C++ | C# |
| JavaScript | TypeScript | Go | Rust |
| Kotlin | Swift | Ruby | PHP |
| SQL | HTML | CSS | Shell |

... and more! See [Languages](docs/LANGUAGES.md) for the full list.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
