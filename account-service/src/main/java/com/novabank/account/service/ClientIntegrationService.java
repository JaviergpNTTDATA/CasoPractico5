package com.novabank.account.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.novabank.account.dto.ClientDTO;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class ClientIntegrationService {

    private final WebClient webClient;

    public ClientIntegrationService(WebClient webClient) {
        this.webClient = webClient;
    }

    @Retry(name = "clienteService", fallbackMethod = "getClientFallback")
    @CircuitBreaker(name = "clienteService", fallbackMethod = "getClientFallback")
    public Mono<ClientDTO> getClient(Long id) {
        return webClient.get()
                // In client-service, the endpoint is /clients/getById/{id} to avoid confusion with account-service's /accounts/{id}.
                .uri("http://client-service/clients/getById/{id}", id)
                .retrieve()
                .bodyToMono(ClientDTO.class);
    }

    public Mono<ClientDTO> getClientFallback(Long id, Throwable ex) {
        log.warn("Fallback activado para cliente {}: {}", id, ex.getMessage());
        ClientDTO dto = new ClientDTO();
        dto.setId(id);
        dto.setFirstName("Client Unavailable");
        dto.setLastName("");
        dto.setDni("");
        dto.setEmail("");
        dto.setPhone("");
        return Mono.just(dto);
    }
}
