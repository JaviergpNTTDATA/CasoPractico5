package com.novabank.account.client;

import org.springframework.stereotype.Component;

import com.novabank.account.dto.ClientDTO;

import reactor.core.publisher.Mono;

/**
 * Fallback legacy de Feign. En esta migración ya no se usa porque la integración
 * con client-service se hace con WebClient + Resilience4j (ver ClientIntegrationService).
 *
 * Se mantiene como componente auxiliar por si se usa desde algún test o wiring antiguo,
 * pero devolviendo Mono para evitar APIs bloqueantes.
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
