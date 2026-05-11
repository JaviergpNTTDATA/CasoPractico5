package com.novabank.exchange.controller;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.novabank.exchange.dto.ExchangeRateResponse;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
public class ExchangeRateController {

    private static final Map<String, BigDecimal> RATES = Map.of(
            "USD->EUR", new BigDecimal("0.92"),
            "EUR->USD", new BigDecimal("1.09"),
            "GBP->EUR", new BigDecimal("1.17"),
            "EUR->GBP", new BigDecimal("0.85"));

    @GetMapping("/exchange-rate")
    public Mono<ExchangeRateResponse> getRate(@RequestParam String from, @RequestParam String to) {
        String key = from.toUpperCase() + "->" + to.toUpperCase();

        BigDecimal rate = RATES.get(key);
        if (rate == null) {
            return Mono.error(new UnsupportedExchangeException("Unsupported currency pair: " + key));
        }

        return Mono.just(new ExchangeRateResponse(from.toUpperCase(), to.toUpperCase(), rate, Instant.now()));
    }

    @org.springframework.web.bind.annotation.ResponseStatus(HttpStatus.BAD_REQUEST)
    static class UnsupportedExchangeException extends RuntimeException {
        UnsupportedExchangeException(String message) {
            super(message);
        }
    }
}
