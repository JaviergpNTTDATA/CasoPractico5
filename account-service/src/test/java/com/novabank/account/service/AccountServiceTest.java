package com.novabank.account.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.novabank.account.dto.AccountDTO;
import com.novabank.account.dto.ClientDTO;
import com.novabank.account.exception.ClientNotFoundException;
import com.novabank.account.model.Account;
import com.novabank.account.repository.AccountRepository;
import com.novabank.account.repository.IbanGenerator;
import com.novabank.account.repository.MovementRepository;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ClientIntegrationService clientIntegrationService;

    @Mock
    private IbanGenerator ibanGenerator;

    @Mock
    private MovementRepository movementRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    void createAccount_withExistingClient_shouldCreateAccount() {
        Long clientId = 1L;

        ClientDTO clientDTO = new ClientDTO();
        clientDTO.setId(clientId);
        clientDTO.setFirstName("Juan");
        clientDTO.setLastName("Pérez");

        when(clientIntegrationService.getClient(clientId)).thenReturn(Mono.just(clientDTO));
        when(ibanGenerator.generateIban()).thenReturn(Mono.just("ES123"));
        when(accountRepository.save(any(Account.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        Mono<AccountDTO> resultMono = accountService.createAccount(clientId);

        StepVerifier.create(resultMono)
                .assertNext(result -> {
                    assertNotNull(result);
                    assertEquals("ES123", result.getIban());
                    assertEquals(clientId, result.getClientId());
                    assertNotNull(result.getBalance());
                    assertEquals(0, BigDecimal.ZERO.compareTo(result.getBalance()));
                })
                .verifyComplete();

        verify(clientIntegrationService).getClient(clientId);
        verify(ibanGenerator).generateIban();
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void createAccount_withFallbackClient_shouldErrorClientNotFound() {
        Long clientId = 99L;

        ClientDTO fallback = new ClientDTO();
        fallback.setId(clientId);
        fallback.setFirstName("No disponible");

        when(clientIntegrationService.getClient(clientId)).thenReturn(Mono.just(fallback));

        StepVerifier.create(accountService.createAccount(clientId))
                .expectError(ClientNotFoundException.class)
                .verify();

        verify(clientIntegrationService).getClient(clientId);
        verify(accountRepository, never()).save(any(Account.class));
    }
}
