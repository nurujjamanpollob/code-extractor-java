package com.example;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.ArrayList;

/**
 * A sample Java class.
 */
public class Sample {
    private final String name;

    public Sample(String name) {
        this.name = name;
    }

    public void process(List<String> items) {
        for (String item : items) {
            System.out.println(name + " processes " + item);
        }
    }

    /**
     * The main method to run the sample.
     * @param args
     */
    @Test
    public static void main(String[] args) {
        Sample s = new Sample("Tester");
        List<String> data = new ArrayList<>();
        data.add("A");
        data.add("B");
        s.process(data);
    }
}