package com.novabank.account.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;

import com.novabank.account.dto.ClientDTO;

import reactor.test.StepVerifier;

@SpringBootTest
@AutoConfigureWireMock(port = 0)
@ActiveProfiles("test")
class ClientServiceClientContractTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        WebClient webClient(WebClient.Builder builder) {
            // En test no usamos discovery. Apuntamos directamente a WireMock.
            return builder.baseUrl("http://localhost:" + System.getProperty("wiremock.server.port"))
                    .build();
        }
    }

    @Autowired
    private ClienteServiceClient clienteServiceClient;

    @Test
    void obtenerCliente_shouldReturnClientFromRemoteService() {
        stubFor(get(urlEqualTo("/clients/1"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "id": 1,
                                  "firstName": "Juan",
                                  "lastName": "Pérez",
                                  "dni": "12345678A",
                                  "email": "juan@example.com",
                                  "phone": "600000000"
                                }
                                """)));

        StepVerifier.create(clienteServiceClient.obtenerCliente(1L))
                .assertNext(client -> {
                    assertNotNull(client);
                    assertEquals(1L, client.getId());
                    assertEquals("Juan", client.getFirstName());
                })
                .verifyComplete();
    }
}
