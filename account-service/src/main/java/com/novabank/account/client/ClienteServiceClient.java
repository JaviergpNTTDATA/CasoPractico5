package com.novabank.account.client;

import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.novabank.account.dto.ClientDTO;
import com.novabank.account.exception.ClientNotFoundException;

import reactor.core.publisher.Mono;

@Component
public class ClienteServiceClient {

    private final WebClient webClient;

    public ClienteServiceClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<ClientDTO> obtenerCliente(Long id) {
        return webClient.get()
                // Debe coincidir con el serviceId registrado en Eureka (normalmente en minúsculas).
                // Con @LoadBalanced, WebClient resolverá "http://client-service" vía Spring Cloud LoadBalancer + Eureka.
                // En client-service el endpoint es /clients/getById/{id} (ver ClientController)
                .uri("http://client-service/clients/getById/{id}", id)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        resp -> Mono.error(new ClientNotFoundException(
                                "Client not found with id: " + id)))
                .bodyToMono(ClientDTO.class);
    }
}
