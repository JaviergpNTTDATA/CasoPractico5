package com.novabank.operation.exception;

public class ExchangeRateRequiredException extends RuntimeException {
    public ExchangeRateRequiredException(String message, Throwable cause) {
        super(message, cause);
    }
}