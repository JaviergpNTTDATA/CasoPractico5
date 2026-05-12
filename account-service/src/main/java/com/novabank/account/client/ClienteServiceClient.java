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
        
                //It needs to match the serviceId registered in Eureka (usually lowercase).
                //With @LoadBalanced, WebClient will resolve "http://client-service" via Spring
                //Cloud LoadBalancer + Eureka.
                .uri("http://client-service/clients/getById/{id}", id)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        resp -> Mono.error(new ClientNotFoundException(
                                "Client not found with id: " + id)))
                .bodyToMono(ClientDTO.class);
    }
}
