package com.extractor.core;

import com.extractor.model.CodeNode;

public interface CodeParser {
    CodeNode parse(String source);
    boolean supports(String language);
}
