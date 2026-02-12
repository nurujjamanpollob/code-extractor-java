package com.extractor;

import com.extractor.core.CodeExtractor;
import com.extractor.model.CodeNode;
import com.extractor.model.NodeType;
import com.extractor.query.CodeQuery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ProductionTest {

    @TempDir
    Path tempDir;

    @Test
    public void testFullExtractionCycle() throws IOException {
        // 1. Prepare Test Files
        Path javaFile = tempDir.resolve("UserService.java");
        Files.writeString(javaFile, "public class UserService { public void saveUser() { int x = 10; } }");

        Path pythonFile = tempDir.resolve("app.py");
        Files.writeString(pythonFile, "class App:\n    def run(self):\n        msg = 'hello'");

        Path htmlFile = tempDir.resolve("index.html");
        Files.writeString(htmlFile, "<html><body><div id='root'></div></body></html>");

        Path sqlFile = tempDir.resolve("schema.sql");
        Files.writeString(sqlFile, "CREATE TABLE users (id INT);");

        // 2. Execute Extraction
        CodeExtractor extractor = new CodeExtractor();
        Map<String, CodeNode> results = extractor.extractFromDirectory(tempDir.toString());

        // 3. Verify Results
        assertNotNull(results);
        assertTrue(results.size() >= 4);

        // Java Verification
        CodeNode javaNode = results.get(javaFile.toAbsolutePath().toString());
        List<CodeNode> javaClasses = javaNode.getClasses();
        assertEquals(1, javaClasses.size());
        assertEquals("UserService", javaClasses.get(0).getName());
        assertEquals(1, javaClasses.get(0).getMethods().size());
        assertEquals("saveUser", javaClasses.get(0).getMethods().get(0).getName());

        // Python Verification
        CodeNode pyNode = results.get(pythonFile.toAbsolutePath().toString());
        assertEquals(1, pyNode.getClasses().size());
        assertEquals("App", pyNode.getClasses().get(0).getName());
        assertEquals(1, pyNode.getClasses().get(0).getFunctions().size());
        assertEquals("run", pyNode.getClasses().get(0).getFunctions().get(0).getName());

        // HTML Verification
        CodeNode htmlNode = results.get(htmlFile.toAbsolutePath().toString());
        List<CodeNode> tags = htmlNode.findByType(NodeType.TAG);
        assertTrue(tags.stream().anyMatch(t -> t.getName().equals("div")));

        // SQL Verification
        CodeNode sqlNode = results.get(sqlFile.toAbsolutePath().toString());
        List<CodeNode> tables = sqlNode.findByType(NodeType.TABLE);
        assertEquals(1, tables.size());
        assertEquals("users", tables.get(0).getName());

        // 4. Test Querying
        List<CodeNode> allMethods = CodeQuery.findMethodsByName(javaNode, "save");
        assertFalse(allMethods.isEmpty());
        assertEquals("saveUser", allMethods.get(0).getName());
        
        System.out.println("Production Test Passed Successfully!");
    }
}
