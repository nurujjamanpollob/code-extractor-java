# Usage Examples

`code-extractor-java` is designed to be simple to integrate. Below are common usage patterns.

## Basic Extraction

The `ExtractionEngine` is the main entry point for extracting code structures.

```java
import com.extractor.ExtractionEngine;
import com.extractor.model.CodeNode;

import java.nio.file.Files;
import java.nio.file.Path;

public class Example {
    public static void main(String[] args) throws Exception {
        String fileName = "MyClass.java";
        String content = Files.readString(Path.of(fileName));

        // Create the engine
        ExtractionEngine engine = new ExtractionEngine();

        // Extract the code structure
        CodeNode root = engine.extract(fileName, content);

        // Print the tree
        printNode(root, 0);
    }

    private static void printNode(CodeNode node, int depth) {
        System.out.println("  ".repeat(depth) + node.getType() + ": " + node.getName());
        for (CodeNode child : node.getChildren()) {
            printNode(child, depth + 1);
        }
    }
}
```

## Querying the Result

You can use the `CodeQuery` utility or built-in methods in `CodeNode` to find specific elements.

```java
// Find all methods in the file
List<CodeNode> methods = root.getMethods();

// Find all classes
List<CodeNode> classes = root.getClasses();

// Find nodes by specific type
List<CodeNode> comments = root.findByType(NodeType.COMMENT);
```

## Exporting to JSON/MCP Format

The library provides a built-in method to convert the extracted tree into a format suitable for Model Context Protocol (MCP) or JSON serialization.

```java
import java.util.Map;
import com.google.gson.Gson; // Assuming you add Gson for serialization

Map<String, Object> mcpData = root.toMcpContext();
String json = new Gson().toJson(mcpData);
System.out.println(json);
```

## Advanced: Custom Language Detection

While the engine automatically detects languages by extension, you can force a specific parser if needed:

```java
import com.extractor.languages.JavaParser;

JavaParser parser = new JavaParser();
CodeNode root = parser.parse("CustomContent.java", content);
```
