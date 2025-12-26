package com.example.expression;

import java.util.List;

/**
 * Demo to test tokenization
 */
public class TokenizerDemo {

    public static void main(String[] args) {
        System.out.println("=== Tokenizer Demo ===\n");

        testTokenization("featureMap.userAge");
        testTokenization("featureMap.userAge > 18");
        testTokenization("5 + 3");
        testTokenization("true && false");
    }

    private static void testTokenization(String expression) {
        System.out.println("Expression: " + expression);
        Tokenizer tokenizer = new Tokenizer(expression);
        List<Token> tokens = tokenizer.tokenize();

        for (Token token : tokens) {
            System.out.println("  " + token);
        }
        System.out.println();
    }
}