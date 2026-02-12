package com.extractor.languages;

import com.extractor.core.ParserRule;
import com.extractor.model.CodeNode;
import com.extractor.model.NodeType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CppParser extends CParser {

    public CppParser() {
        super("cpp");
    }

    @Override
    protected List<ParserRule> createRules() {
        List<ParserRule> rules = new ArrayList<>();
        
        // C++ specific rules with high priority
        rules.add((ctx, pos) -> Optional.ofNullable(extractIdentifierAfterKeyword(stripComments(ctx).trim(), "namespace"))
                .map(name -> new CodeNode(NodeType.NAMESPACE, name, pos)));
        
        rules.add((ctx, pos) -> Optional.ofNullable(extractIdentifierAfterKeyword(stripComments(ctx).trim(), "class"))
                .map(name -> new CodeNode(NodeType.CLASS, name, pos)));
        
        // Add rules from CParser (struct, enum, method)
        rules.addAll(super.createRules());
        
        return rules;
    }

    @Override
    protected boolean isMethodDeclaration(String text) {
        // C++ methods can have more complex markers
        String clean = stripComments(text).trim();
        if (clean.contains("class ") || clean.contains("struct ") || clean.contains("namespace ")) {
            if (clean.matches("(?s).*\\b(class|struct|namespace)\\s+[a-zA-Z_].*")) {
                return false;
            }
        }
        return super.isMethodDeclaration(text);
    }
}
