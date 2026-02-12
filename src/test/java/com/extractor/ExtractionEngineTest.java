package com.extractor;

import com.extractor.model.CodeNode;
import com.extractor.model.NodeType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class ExtractionEngineTest {

    @Test
    public void testJavaExtraction() {
        ExtractionEngine engine = new ExtractionEngine();
        String code = "package com.test;\n" +
                      "public class MyClass {\n" +
                      "    public void myMethod() {\n" +
                      "        System.out.println(\"Hello\");\n" +
                      "    }\n" +
                      "}";
        
        CodeNode root = engine.extract("MyClass.java", code);
        
        List<CodeNode> classes = root.findByType(NodeType.CLASS);
        assertEquals(1, classes.size());
        assertEquals("MyClass", classes.get(0).getName());
        
        List<CodeNode> methods = root.findByType(NodeType.METHOD);
        assertEquals(1, methods.size());
        assertEquals("myMethod", methods.get(0).getName());
    }

    @Test
    public void testFaultTolerance() {
        ExtractionEngine engine = new ExtractionEngine();
        // Missing closing brace for method
        String code = "public class MyClass {\n" +
                      "    public void myMethod() {\n" +
                      "        if(true) { \n" +
                      "    }\n" +
                      "}";
        
        CodeNode root = engine.extract("MyClass.java", code);
        assertNotNull(root);
        List<CodeNode> methods = root.findByType(NodeType.METHOD);
        assertFalse(methods.isEmpty());
    }

    @Test
    public void testWebExtraction() {
        ExtractionEngine engine = new ExtractionEngine();
        String html = "<html>\n" +
                      "  <head>\n" +
                      "    <style>\n" +
                      "      .container { color: red; }\n" +
                      "      #header { font-size: 20px; }\n" +
                      "    </style>\n" +
                      "  </head>\n" +
                      "  <body>\n" +
                      "    <div class='container'>Hello</div>\n" +
                      "  </body>\n" +
                      "</html>";
        
        CodeNode root = engine.extract("index.html", html);
        
        List<CodeNode> tags = root.findByType(NodeType.TAG);
        assertTrue(tags.stream().anyMatch(t -> t.getName().equals("div")));
        
        List<CodeNode> selectors = root.findByType(NodeType.SELECTOR);
        assertEquals(2, selectors.size());
        assertTrue(selectors.stream().anyMatch(s -> s.getName().equals(".container")));
    }
}
