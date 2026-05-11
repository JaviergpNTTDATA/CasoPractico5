package com.novabank.client.controller;

import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.novabank.client.dto.ClientDTO;
import com.novabank.client.service.ClientService;

import reactor.core.publisher.Flux;

@WebFluxTest(controllers = ClientController.class)
class ClientControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ClientService clientService;

    @Test
    void listClients_shouldReturnJsonArray() {
        ClientDTO dto = ClientDTO.builder()
                .id(1L)
                .firstName("Juan")
                .lastName("Pérez")
                .dni("12345678A")
                .email("juan@example.com")
                .phone("600000000")
                .accountCount(0)
                .build();

        when(clientService.listClients()).thenReturn(Flux.just(dto));

        webTestClient.get()
                .uri("/clients/getAll")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$[0].id").isEqualTo(1)
                .jsonPath("$[0].firstName").isEqualTo("Juan");
    }
}
