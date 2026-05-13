package com.novabank.operation.client;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * DTO for the exchange-rate-mock-service response.
 */
public record ExchangeRateResponse(String from, String to, BigDecimal rate, Instant timestamp) {
}
