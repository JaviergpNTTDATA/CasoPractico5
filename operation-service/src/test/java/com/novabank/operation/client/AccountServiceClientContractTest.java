package com.novabank.operation.client;

import com.novabank.operation.dto.AccountDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Disabled;

@SpringBootTest(properties = {
        // Forzamos el baseUrl del AccountServiceClient a WireMock
        "account-service.base-url=http://localhost:${wiremock.server.port}"
})
@AutoConfigureWireMock(port = 0)
@ActiveProfiles("test")
@org.junit.jupiter.api.Disabled("Contrato legacy: depende de la configuración LoadBalanced/Eureka; se testea con tests unitarios de cliente o integración completa")
class AccountServiceClientContractTest {

    @Autowired
    private AccountServiceClient accountServiceClient;

    @Test
    void getAccountByIban_shouldReturnAccountFromRemoteService() {
        stubFor(get(urlEqualTo("/accounts/iban/ES123"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            {
                              "id": 1,
                              "iban": "ES123",
                              "balance": 100.50
                            }
                            """)));

        reactor.test.StepVerifier.create(accountServiceClient.getAccountByIban("ES123"))
                .assertNext(account -> {
                    assertNotNull(account);
                    assertEquals("ES123", account.iban());
                    assertEquals(1L, account.id());
                })
                .verifyComplete();
    }
}
