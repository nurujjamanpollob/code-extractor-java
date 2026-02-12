package com.extractor;

import com.extractor.model.CodeNode;
import com.extractor.model.NodeType;
import com.extractor.query.CodeQuery;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        ExtractionEngine engine = new ExtractionEngine();

        // 1. Java Extraction Example with Comments
        String javaCode = "package com.demo;\n" +
                          "/** User service for handling operations */\n" +
                          "public class UserService {\n" +
                          "    // Returns a user by ID\n" +
                          "    public User getUser(Long id) {\n" +
                          "        return repo.findById(id);\n" +
                          "    }\n" +
                          "}";

        System.out.println("--- Parsing Java ---");
        CodeNode javaRoot = engine.extract("UserService.java", javaCode);
        javaRoot.getClasses().forEach(n -> System.out.println("Found Class: " + n.getName()));
        javaRoot.getMethods().forEach(n -> System.out.println("Found Method: " + n.getName()));
        javaRoot.getComments().forEach(n -> System.out.println("Found Comment: " + n.getContent().trim()));

        // 2. MCP Integration Demo
        System.out.println("\n--- MCP Context Representation ---");
        Map<String, Object> mcpContext = javaRoot.toMcpContext();
        System.out.println("MCP Context Map generated for " + javaRoot.getName());
        // In a real MCP server, this map would be serialized to JSON

        // 3. Python Extraction Example
        String pythonCode = "class Controller:\n" +
                            "    # Handle incoming requests\n" +
                            "    def handle_request(self):\n" +
                            "        print('Handling')\n" +
                            "\n" +
                            "def global_util():\n" +
                            "    pass";

        System.out.println("\n--- Parsing Python ---");
        CodeNode pyRoot = engine.extract("api.py", pythonCode);
        pyRoot.getClasses().forEach(n -> System.out.println("Found Class: " + n.getName()));
        pyRoot.getFunctions().forEach(n -> System.out.println("Found Function: " + n.getName()));
        pyRoot.getComments().forEach(n -> System.out.println("Found Comment: " + n.getContent().trim()));

        // 4. SQL Extraction Example
        String sqlCode = "CREATE TABLE Orders (id INT PRIMARY KEY, total DECIMAL);\n" +
                         "-- Process orders in background\n" +
                         "CREATE PROCEDURE ProcessOrder() BEGIN UPDATE Orders SET status='done'; END;";

        System.out.println("\n--- Parsing SQL ---");
        CodeNode sqlRoot = engine.extract("schema.sql", sqlCode);
        sqlRoot.findByType(NodeType.TABLE).forEach(n -> System.out.println("Found Table: " + n.getName()));
        sqlRoot.findByType(NodeType.PROCEDURE).forEach(n -> System.out.println("Found Procedure: " + n.getName()));
        sqlRoot.getComments().forEach(n -> System.out.println("Found Comment: " + n.getContent().trim()));

        // 5. Query Utility Demo
        System.out.println("\n--- Query API Demo ---");
        List<CodeNode> matches = CodeQuery.findMethodsByName(javaRoot, "get");
        matches.forEach(m -> System.out.println("Query Match: " + m.getName()));
    }
}
