package com.novabank.operation.client;

/**
 * Raised when exchange-rate-mock-service cannot be reached or returns an error.
 */
public class ExchangeRateUnavailableException extends RuntimeException {

    public ExchangeRateUnavailableException(String message) {
        super(message);
    }

    public ExchangeRateUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
