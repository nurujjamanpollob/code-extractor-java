package com.extractor.core;

import com.extractor.model.CodeNode;
import java.util.Optional;

@FunctionalInterface
public interface ParserRule {
    Optional<CodeNode> apply(String context, int position);
}
