package com.example.expression;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tokenizes expressions into a stream of tokens
 */
public class Tokenizer {

    // Regex patterns for different token types
    private static final Pattern NUMBER_PATTERN = Pattern.compile("^\\d+(\\.\\d+)?");
    private static final Pattern STRING_PATTERN = Pattern.compile("^([\"'])([^\"']*?)\\1");
    private static final Pattern IDENTIFIER_PATTERN = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*");
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("^\\s+");

    private final String input;
    private int position;

    public Tokenizer(String input) {
        this.input = input != null ? input.trim() : "";
        this.position = 0;
    }

    /**
     * Tokenizes the entire input string
     */
    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();
        position = 0;

        while (position < input.length()) {
            Token token = nextToken();
            if (token.getType() != Token.TokenType.UNKNOWN) {
                tokens.add(token);
            }
        }

        // Add EOF token
        tokens.add(new Token(Token.TokenType.EOF, "", position));
        return tokens;
    }

    /**
     * Gets the next token from the input
     */
    private Token nextToken() {
        // Skip whitespace
        skipWhitespace();

        if (position >= input.length()) {
            return new Token(Token.TokenType.EOF, "", position);
        }

        int startPos = position;
        char current = input.charAt(position);

        // Handle two-character operators first
        if (position + 1 < input.length()) {
            String twoChar = input.substring(position, position + 2);
            Token.TokenType twoCharType = getTwoCharOperatorType(twoChar);
            if (twoCharType != null) {
                position += 2;
                return new Token(twoCharType, twoChar, startPos);
            }
        }

        // Handle single-character operators and punctuation
        Token.TokenType singleCharType = getSingleCharOperatorType(current);
        if (singleCharType != null) {
            position++;
            return new Token(singleCharType, String.valueOf(current), startPos);
        }

        // Handle numbers
        Matcher numberMatcher = NUMBER_PATTERN.matcher(input.substring(position));
        if (numberMatcher.find()) {
            String numberStr = numberMatcher.group();
            position += numberStr.length();
            return new Token(Token.TokenType.NUMBER, numberStr, startPos);
        }

        // Handle strings
        Matcher stringMatcher = STRING_PATTERN.matcher(input.substring(position));
        if (stringMatcher.find()) {
            String fullMatch = stringMatcher.group();
            String content = stringMatcher.group(2); // The content without quotes
            position += fullMatch.length();
            return new Token(Token.TokenType.STRING, content, startPos);
        }

        // Handle identifiers and keywords
        Matcher identifierMatcher = IDENTIFIER_PATTERN.matcher(input.substring(position));
        if (identifierMatcher.find()) {
            String identifier = identifierMatcher.group();
            position += identifier.length();

            // Check for boolean keywords
            if ("true".equals(identifier) || "false".equals(identifier)) {
                return new Token(Token.TokenType.BOOLEAN, identifier, startPos);
            }

            return new Token(Token.TokenType.IDENTIFIER, identifier, startPos);
        }

        // Unknown character
        position++;
        return new Token(Token.TokenType.UNKNOWN, String.valueOf(current), startPos);
    }

    /**
     * Skips whitespace characters
     */
    private void skipWhitespace() {
        while (position < input.length() && Character.isWhitespace(input.charAt(position))) {
            position++;
        }
    }

    /**
     * Gets token type for two-character operators
     */
    private Token.TokenType getTwoCharOperatorType(String twoChar) {
        switch (twoChar) {
            case "<=": return Token.TokenType.LESS_EQUAL;
            case ">=": return Token.TokenType.GREATER_EQUAL;
            case "==": return Token.TokenType.EQUAL;
            case "!=": return Token.TokenType.NOT_EQUAL;
            case "&&": return Token.TokenType.AND;
            case "||": return Token.TokenType.OR;
            default: return null;
        }
    }

    /**
     * Gets token type for single-character operators and punctuation
     */
    private Token.TokenType getSingleCharOperatorType(char ch) {
        switch (ch) {
            case '+': return Token.TokenType.PLUS;
            case '-': return Token.TokenType.MINUS;
            case '*': return Token.TokenType.MULTIPLY;
            case '/': return Token.TokenType.DIVIDE;
            case '%': return Token.TokenType.MODULUS;
            case '<': return Token.TokenType.LESS_THAN;
            case '>': return Token.TokenType.GREATER_THAN;
            case '!': return Token.TokenType.NOT;
            case '(': return Token.TokenType.LEFT_PAREN;
            case ')': return Token.TokenType.RIGHT_PAREN;
            case '.': return Token.TokenType.DOT;
            default: return null;
        }
    }

    /**
     * Converts token to operator symbol for OperatorFactory
     */
    public static String tokenToOperatorSymbol(Token token) {
        switch (token.getType()) {
            case PLUS: return "+";
            case MINUS: return "-";
            case MULTIPLY: return "*";
            case DIVIDE: return "/";
            case MODULUS: return "%";
            case LESS_THAN: return "<";
            case LESS_EQUAL: return "<=";
            case GREATER_THAN: return ">";
            case GREATER_EQUAL: return ">=";
            case EQUAL: return "==";
            case NOT_EQUAL: return "!=";
            case AND: return "&&";
            case OR: return "||";
            case NOT: return "!";
            default:
                throw new IllegalArgumentException("Token is not an operator: " + token.getType());
        }
    }
}