package com.extractor.comprehensive;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Arrays;

/**
 * Block comment at the top of the file.
 */
@Deprecated
@SuppressWarnings("unchecked")
public class Comprehensive<T> {

    // Single-line comment for a field
    private List<T> items;

    /**
     * Constructor block comment.
     */
    public Comprehensive() {
        this.items = (List<T>) new java.util.ArrayList<>();
    }

    /*
     * Block comment for a method
     */
    @Override
    @Deprecated
    public String toString() {
        return "Comprehensive{" +
                "items=" + items +
                '}';
    }

    public void processItems() {
        // Lambda expression
        items.forEach(item -> {
            System.out.println(item);
        });

        // Stream API
        List<String> results = items.stream()
                .filter(i -> i != null)
                .map(Object::toString)
                .collect(Collectors.toList());
    }

    @interface CustomAnnotation {
        String value() default "";
    }

    public static class InnerClass {
        public void innerMethod() {
            // Nested block
            {
                int x = 10;
            }
        }
    }

    static {
        // Static initialization block
        System.out.println("Static block");
    }
}
