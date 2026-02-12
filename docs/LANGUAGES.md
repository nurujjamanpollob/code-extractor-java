# Supported Programming Languages

`code-extractor-java` supports a wide range of programming languages and formats. The library uses a rule-based approach to identify structural elements like classes, functions, interfaces, and comments.

## Language Support Matrix

| Language | Extension(s) | Key Structures Extracted |
| :--- | :--- | :--- |
| **Java** | `.java` | Classes, Interfaces, Enums, Methods, Annotations, Imports |
| **Python** | `.py` | Classes, Functions (def), Imports |
| **C++** | `.cpp`, `.h`, `.hpp` | Classes, Structs, Functions, Namespaces, Includes |
| **C** | `.c`, `.h` | Functions, Structs, Includes |
| **C#** | `.cs` | Classes, Namespaces, Methods, Properties |
| **JavaScript** | `.js`, `.mjs` | Classes, Functions, Const/Let exports |
| **TypeScript** | `.ts`, `.tsx` | Classes, Interfaces, Types, Functions |
| **Go** | `.go` | Functions, Structs, Interfaces, Packages |
| **Rust** | `.rs` | Structs, Enums, Functions (fn), Traits, Mods |
| **Kotlin** | `.kt` | Classes, Objects, Functions |
| **Swift** | `.swift` | Classes, Structs, Protocols, Extensions, Functions |
| **Ruby** | `.rb` | Classes, Modules, Methods |
| **PHP** | `.php` | Classes, Interfaces, Functions |
| **Dart** | `.dart` | Classes, Methods |
| **Scala** | `.scala` | Classes, Objects, Traits, Defs |
| **SQL** | `.sql` | Tables, Procedures, Views (Basic) |
| **HTML** | `.html`, `.htm` | Tags, Attributes |
| **CSS** | `.css` | Selectors, Rule blocks |
| **Shell** | `.sh`, `.bash` | Functions |
| **Objective-C** | `.m`, `.mm` | Interfaces, Implementations, Methods |
| **Zig** | `.zig` | Functions, Structs |
| **Elixir** | `.ex`, `.exs` | Modules, Functions |
| **GDScript** | `.gd` | Classes, Functions |

## Adding a New Language

To add support for a new language:
1. Create a new parser class in `com.extractor.languages` that extends `BaseParser`.
2. Implement the `parse` and `supports` methods.
3. Register the parser in `ExtractionEngine`.
4. Add the file extension to `LanguageDetector`.
