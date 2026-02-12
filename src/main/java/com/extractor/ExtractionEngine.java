package com.extractor;

import com.extractor.core.CodeParser;
import com.extractor.core.LanguageDetector;
import com.extractor.languages.*;
import com.extractor.model.CodeNode;

import java.util.ArrayList;
import java.util.List;

public class ExtractionEngine {
    private final List<CodeParser> parsers = new ArrayList<>();

    public ExtractionEngine() {
        parsers.add(new JavaParser());
        parsers.add(new HTMLParser());
        parsers.add(new CSSParser());
        parsers.add(new JSParser());
        parsers.add(new TSParser());
        parsers.add(new PythonParser());
        parsers.add(new GoParser());
        parsers.add(new RustParser());
        parsers.add(new CSharpParser());
        parsers.add(new SqlParser());
        parsers.add(new CParser());
        parsers.add(new CppParser());
        parsers.add(new RubyParser());
        parsers.add(new PHPParser());
        parsers.add(new KotlinParser());
        parsers.add(new DartParser());
        parsers.add(new ShellParser());
        parsers.add(new ZigParser());
        parsers.add(new GDScriptParser());
        parsers.add(new ScalaParser());
        parsers.add(new ElixirParser());
        parsers.add(new SwiftParser());
        parsers.add(new ObjectiveCParser());
    }

    public CodeNode extract(String fileName, String source) {
        String language = LanguageDetector.detect(fileName, source);
        for (CodeParser parser : parsers) {
            if (parser.supports(language)) {
                return parser.parse(source);
            }
        }
        // Fallback to a generic parser or return root with no children
        return new JavaParser().parse(source); 
    }
}
