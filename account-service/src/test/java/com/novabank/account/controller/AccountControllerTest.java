package com.novabank.account.controller;

import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.novabank.account.dto.AccountDTO;
import com.novabank.account.service.AccountService;
import com.novabank.account.service.InquiryService;

import reactor.core.publisher.Flux;

@WebFluxTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private AccountService accountService;

    @MockBean
    private InquiryService inquiryService;

    @Test
    void listByClient_shouldReturnAccounts() {
        AccountDTO dto = new AccountDTO();
        dto.setClientId(1L);
        dto.setIban("ES123");
        dto.setBalance(BigDecimal.ZERO);
        dto.setCreatedAt(LocalDateTime.now());

        when(accountService.listClientAccounts(1L)).thenReturn(Flux.just(dto));

        webTestClient.get()
                .uri("/accounts/client/1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].iban").isEqualTo("ES123")
                .jsonPath("$[0].clientId").isEqualTo(1)
                .jsonPath("$[0].balance").isEqualTo(0);
    }
}
