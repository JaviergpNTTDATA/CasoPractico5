package com.novabank.account.client;

import org.springframework.stereotype.Component;

import com.novabank.account.dto.ClientDTO;

import reactor.core.publisher.Mono;


/**
 * Fallback legacy of feign. In this migration it is no longer used because the integration with client-service is done with WebClient + Resilience4j (see ClientIntegrationService).
 * It is maintained as an auxiliary component in case it is used from some test or old wiring, but returning Mono to avoid blocking APIs.
 */
@Component
public class ClientServiceFallback {

    public Mono<ClientDTO> getClientById(Long id) {
        ClientDTO dto = new ClientDTO();
        dto.setId(id);
        dto.setFirstName("Client Unavailable");
        dto.setLastName("");
        dto.setDni("");
        dto.setEmail("");
        dto.setPhone("");
        dto.setAccountCount(0);
        return Mono.just(dto);
    }
}
