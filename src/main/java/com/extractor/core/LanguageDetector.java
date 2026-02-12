package com.extractor.core;

import java.util.HashMap;
import java.util.Map;

public class LanguageDetector {
    private static final Map<String, String> EXTENSION_MAP = new HashMap<>();

    static {
        // Web
        EXTENSION_MAP.put("html", "html");
        EXTENSION_MAP.put("htm", "html");
        EXTENSION_MAP.put("css", "css");
        EXTENSION_MAP.put("js", "javascript");
        EXTENSION_MAP.put("ts", "typescript");
        EXTENSION_MAP.put("php", "php");
        
        // JVM
        EXTENSION_MAP.put("java", "java");
        EXTENSION_MAP.put("kt", "kotlin");
        EXTENSION_MAP.put("scala", "scala");
        EXTENSION_MAP.put("groovy", "groovy");
        
        // Python & Scripting
        EXTENSION_MAP.put("py", "python");
        EXTENSION_MAP.put("sh", "bash");
        EXTENSION_MAP.put("bash", "bash");
        EXTENSION_MAP.put("ps1", "powershell");
        EXTENSION_MAP.put("mojo", "mojo");
        EXTENSION_MAP.put("r", "r");
        EXTENSION_MAP.put("jl", "julia");
        
        // Systems & C-Family
        EXTENSION_MAP.put("c", "c");
        EXTENSION_MAP.put("cpp", "cpp");
        EXTENSION_MAP.put("cc", "cpp");
        EXTENSION_MAP.put("h", "cpp");
        EXTENSION_MAP.put("cs", "csharp");
        EXTENSION_MAP.put("go", "go");
        EXTENSION_MAP.put("rs", "rust");
        EXTENSION_MAP.put("zig", "zig");
        
        // Mobile & Other
        EXTENSION_MAP.put("swift", "swift");
        EXTENSION_MAP.put("m", "objectivec");
        EXTENSION_MAP.put("dart", "dart");
        EXTENSION_MAP.put("rb", "ruby");
        
        // SQL & Data
        EXTENSION_MAP.put("sql", "sql");
        
        // Functional
        EXTENSION_MAP.put("ex", "elixir");
        EXTENSION_MAP.put("exs", "elixir");
        EXTENSION_MAP.put("erl", "erlang");
        EXTENSION_MAP.put("hs", "haskell");
        
        // Godot
        EXTENSION_MAP.put("gd", "gdscript");
    }

    public static String detect(String fileName, String content) {
        if (fileName != null && fileName.contains(".")) {
            String ext = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
            if (EXTENSION_MAP.containsKey(ext)) {
                return EXTENSION_MAP.get(ext);
            }
        }

        // Content-based fallback
        if (content != null) {
            if (content.contains("public class ") || content.contains("package ")) return "java";
            if (content.contains("def ") && content.contains(":")) return "python";
            if (content.contains("function ") || content.contains("const ")) return "javascript";
            if (content.contains("<html>") || content.contains("<!DOCTYPE html>")) return "html";
            if (content.contains("<?php")) return "php";
            if (content.contains("package main") && content.contains("func ")) return "go";
            if (content.contains("fn main()") || content.contains("pub fn ")) return "rust";
            if (content.contains("using System;") || content.contains("namespace ")) return "csharp";
            if (content.contains("SELECT ") && content.contains("FROM ")) return "sql";
        }

        return "unknown";
    }
}
