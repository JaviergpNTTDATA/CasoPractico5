package com.novabank.operation.client;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.novabank.operation.dto.AccountDTO;
import com.novabank.operation.dto.MovementDTO;
import com.novabank.operation.exception.AccountNotFoundException;

import reactor.core.publisher.Mono;

@Component
public class AccountServiceClient {

    private final WebClient webClient;

    public AccountServiceClient(@LoadBalanced WebClient.Builder builder,
                                @Value("${account-service.base-url:http://ACCOUNT-SERVICE}") String baseUrl) {
        // baseUrl por defecto = serviceId Eureka. En tests se puede overridear (WireMock).
        this.webClient = builder.baseUrl(baseUrl).build();
    }

    public Mono<AccountDTO> getAccountByIban(String iban) {
        return webClient.get()
                .uri("/accounts/iban/{iban}", iban)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        resp -> Mono.error(new AccountNotFoundException("Account not found with IBAN: " + iban)))
                .bodyToMono(AccountDTO.class);
    }

    public Mono<MovementDTO> deposit(String iban, BigDecimal amount) {
        return webClient.post()
                .uri(uriBuilder -> uriBuilder.path("/accounts/{iban}/deposit")
                        .queryParam("amount", amount)
                        .build(iban))
                .retrieve()
                .bodyToMono(MovementDTO.class);
    }

    public Mono<MovementDTO> withdraw(String iban, BigDecimal amount) {
        return webClient.post()
                .uri(uriBuilder -> uriBuilder.path("/accounts/{iban}/withdraw")
                        .queryParam("amount", amount)
                        .build(iban))
                .retrieve()
                .bodyToMono(MovementDTO.class);
    }

    public Mono<MovementDTO> transfer(String originIban, String destinationIban, BigDecimal amount) {
        return webClient.post()
                .uri(uriBuilder -> uriBuilder.path("/accounts/transfer")
                        .queryParam("originIban", originIban)
                        .queryParam("destinationIban", destinationIban)
                        .queryParam("amount", amount)
                        .build())
                .retrieve()
                .bodyToMono(MovementDTO.class);
    }
}
