package com.novabank.operation.service;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.novabank.operation.client.AccountServiceClient;
import com.novabank.operation.client.ExchangeRateClient;
import com.novabank.operation.dto.AccountDTO;
import com.novabank.operation.dto.MovementDTO;
import com.novabank.operation.dto.TransferRequest;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OperationServiceTest {

    @Mock
    private AccountServiceClient accountServiceClient;

    @Mock
    private ExchangeRateClient exchangeRateClient;

    @InjectMocks
    private OperationService operationService;

    @Test
    void transfer_withEnoughBalance_shouldReturnTransferDto() {
        TransferRequest request = new TransferRequest(
                "ESFROM",
                "ESTO",
                BigDecimal.TEN
        );

        AccountDTO source = new AccountDTO(
                1L,
                "ESFROM",
                BigDecimal.valueOf(100)
        );

        AccountDTO target = new AccountDTO(
                2L,
                "ESTO",
                BigDecimal.ZERO
        );

        // La implementación actual consulta divisa antes de transferir.
        when(exchangeRateClient.getRate("EUR", "EUR")).thenReturn(Mono.just(
                new ExchangeRateClient.ExchangeRateResponse("EUR", "EUR", BigDecimal.ONE, java.time.Instant.now())
        ));

        when(accountServiceClient.getAccountByIban("ESFROM")).thenReturn(Mono.just(source));
        when(accountServiceClient.getAccountByIban("ESTO")).thenReturn(Mono.just(target));

        // account-service transfer devuelve un MovementDTO (ver AccountServiceClient.transfer)
        when(accountServiceClient.transfer("ESFROM", "ESTO", BigDecimal.TEN))
                .thenReturn(Mono.just(new MovementDTO(
                        1L,
                        "ESFROM",
                        "TRANSFER",
                        BigDecimal.TEN,
                        java.time.LocalDateTime.now()
                )));

        StepVerifier.create(operationService.transfer(request))
                .assertNext(result -> {
                    org.junit.jupiter.api.Assertions.assertEquals("ESFROM", result.sourceIban());
                    org.junit.jupiter.api.Assertions.assertEquals("ESTO", result.targetIban());
                    org.junit.jupiter.api.Assertions.assertEquals(0, BigDecimal.TEN.compareTo(result.amount()));
                })
                .verifyComplete();

        verify(exchangeRateClient).getRate("EUR", "EUR");
        verify(accountServiceClient).getAccountByIban("ESFROM");
        verify(accountServiceClient).getAccountByIban("ESTO");
        verify(accountServiceClient).transfer("ESFROM", "ESTO", BigDecimal.TEN);
        verifyNoMoreInteractions(accountServiceClient, exchangeRateClient);
    }
}
