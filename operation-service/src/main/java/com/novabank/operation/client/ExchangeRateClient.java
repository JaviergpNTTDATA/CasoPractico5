package com.novabank.operation.client;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;

import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;

import reactor.core.publisher.Mono;

@Component
public class ExchangeRateClient {

    private final WebClient webClient;

    public ExchangeRateClient(WebClient.Builder loadBalancedWebClientBuilder) {
        // Requiere spring-cloud-starter-loadbalancer + @LoadBalanced en el builder
        this.webClient = loadBalancedWebClientBuilder.build();
    }

    public Mono<ExchangeRateResponse> getRate(String from, String to) {
        return webClient.get()
                .uri("http://EXCHANGE-RATE-MOCK-SERVICE/exchange/rate?from={from}&to={to}",
                        from, to)
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp -> resp.bodyToMono(String.class)
                        .defaultIfEmpty("Exchange rate service error")
                        .flatMap(body -> Mono.error(new ExchangeRateUnavailableException(body))))
                .bodyToMono(ExchangeRateResponse.class)
                .timeout(Duration.ofSeconds(2))
                .onErrorMap(WebClientRequestException.class,
                        ex -> new ExchangeRateUnavailableException("Exchange rate service unreachable", ex));
    }

    public record ExchangeRateResponse(String from, String to, BigDecimal rate, Instant timestamp) {
    }

    public static class ExchangeRateUnavailableException extends RuntimeException {
        public ExchangeRateUnavailableException(String message) {
            super(message);
        }

        public ExchangeRateUnavailableException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
