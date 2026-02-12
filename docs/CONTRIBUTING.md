# Contributing to Code Extractor Java

Thank you for your interest in contributing! This project aims to provide the most reliable multi-language code structure extraction tool.

## How to Contribute

### 1. Reporting Bugs
- Open an issue on GitHub.
- Provide a sample of the code that caused the issue.
- Describe the expected vs. actual output.

### 2. Suggesting Features
- Open an issue describing the use case.
- For new language support, provide samples of typical code structures for that language.

### 3. Submitting Pull Requests
- Fork the repository.
- Create a feature branch (`git checkout -b feature/amazing-feature`).
- Ensure all tests pass (`./gradlew test`).
- Add tests for new functionality.
- Commit your changes (`git commit -m 'Add some amazing feature'`).
- Push to the branch (`git push origin feature/amazing-feature`).
- Open a Pull Request.

## Development Guidelines

### Adding Support for a New Language
1. Create a test file in `src/test/resources/samples/` (e.g., `sample.xyz`).
2. Create a test class in `src/test/java/com/extractor/languages/` that inherits from a base test class if available.
3. Implement the parser in `src/main/java/com/extractor/languages/`. Use existing parsers (like `JavaParser` or `PythonParser`) as templates.
4. Register the new parser in `ExtractionEngine`.

### Code Style
- Follow standard Java Google Style Guide.
- Use meaningful variable names.
- Document complex regex patterns or parsing logic with comments.

### Testing
We use JUnit 5 for testing. Every parser should have a corresponding test that verifies:
- Class extraction.
- Method/Function extraction.
- Nested structure handling.
- Comment preservation.
- String literal skipping.
