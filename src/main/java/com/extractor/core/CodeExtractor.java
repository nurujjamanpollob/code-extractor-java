package com.extractor.core;

import com.extractor.ExtractionEngine;
import com.extractor.model.CodeNode;
import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class CodeExtractor {
    private final ExtractionEngine engine;

    public CodeExtractor() {
        this.engine = new ExtractionEngine();
    }

    public Map<String, CodeNode> extractFromDirectory(String directoryPath) throws IOException {
        Map<String, CodeNode> results = new HashMap<>();
        Path root = Paths.get(directoryPath);

        if (!Files.exists(root)) {
            throw new IOException("Directory does not exist: " + directoryPath);
        }

        try (Stream<Path> paths = Files.walk(root)) {
            paths.filter(Files::isRegularFile)
                 .forEach(path -> {
                     try {
                         String content = Files.readString(path);
                         String fileName = path.getFileName().toString();
                         CodeNode node = engine.extract(fileName, content);
                         results.put(path.toAbsolutePath().toString(), node);
                     } catch (IOException e) {
                         System.err.println("Failed to read file: " + path + " - " + e.getMessage());
                     }
                 });
        }
        return results;
    }

    public CodeNode extractFromFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        String content = Files.readString(path);
        String fileName = path.getFileName().toString();
        return engine.extract(fileName, content);
    }
}
