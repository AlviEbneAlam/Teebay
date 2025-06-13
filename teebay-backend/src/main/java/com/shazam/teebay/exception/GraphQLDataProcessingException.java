package com.shazam.teebay.exception;

public class GraphQLDataProcessingException extends RuntimeException {
    public GraphQLDataProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
