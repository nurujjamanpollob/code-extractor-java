# Configuration & Output Formats

`code-extractor-java` provides flexibility in how extracted data is structured and presented.

## Output Formats

### 1. In-Memory Tree (`CodeNode`)
The primary output is a tree of `CodeNode` objects. This is ideal for programmatic analysis within Java applications.

### 2. MCP Map Format (`toMcpContext`)
The `CodeNode.toMcpContext()` method generates a nested `Map<String, Object>` structure specifically designed for the **Model Context Protocol**. 

Structure of the map:
- `name`: Element name.
- `type`: Element type.
- `content`: The raw source code.
- `children`: List of child maps.
- `metadata`: A map containing `lineStart`, `lineEnd`, and `charCount`.

### 3. JSON Export
While the library doesn't include a JSON library (to keep dependencies minimal), the `Map` returned by `toMcpContext()` is fully compatible with popular JSON libraries like Jackson or Gson.

```java
// Example with Jackson
ObjectMapper mapper = new ObjectMapper();
String json = mapper.writeValueAsString(root.toMcpContext());
```

## Extraction Settings

The extraction process can be customized by modifying the `ExtractionEngine` or individual parsers (programmatically).

### Filtering Node Types
By default, all structures are extracted. You can filter the tree after extraction:

```java
CodeNode root = engine.extract(file, content);
List<CodeNode> methodsOnly = root.getChildren().stream()
    .filter(n -> n.getType() == NodeType.METHOD)
    .collect(Collectors.toList());
```

### Depth Control
You can limit the depth of the returned tree by traversing the `CodeNode` structure and pruning nodes beyond a certain level.

## Language Mapping
If you have a custom file extension that should be treated as a specific language, you can register it in the `LanguageDetector`.

```java
// Example: Treat .custom as Java
LanguageDetector.registerExtension(".custom", "java");
```
