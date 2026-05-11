package com.novabank.client.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.novabank.client.dto.CreateClient;
import com.novabank.client.model.Client;
import com.novabank.client.repository.ClientRepository;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ClientService clientService;

    @Test
    void createClient_shouldPersistAndReturnDto() {
        CreateClient createClient = new CreateClient();
        createClient.setFirstName("Juan");
        createClient.setLastName("Pérez");
        createClient.setDni("12345678A");
        createClient.setEmail("juan@example.com");
        createClient.setPhone("600000000");

        Client client = new Client();
        client.setId(1L);
        client.setFirstName("Juan");
        client.setLastName("Pérez");
        client.setDni("12345678A");
        client.setEmail("juan@example.com");
        client.setPhone("600000000");

        when(clientRepository.existsByDni("12345678A")).thenReturn(Mono.just(false));
        when(clientRepository.existsByEmail("juan@example.com")).thenReturn(Mono.just(false));
        when(clientRepository.existsByPhone("600000000")).thenReturn(Mono.just(false));
        when(clientRepository.save(any(Client.class))).thenReturn(Mono.just(client));

        StepVerifier.create(clientService.createClient(createClient))
                .assertNext(dto -> {
                    assertNotNull(dto);
                    assertEquals(1L, dto.getId());
                    assertEquals("Juan", dto.getFirstName());
                })
                .verifyComplete();

        ArgumentCaptor<Client> captor = ArgumentCaptor.forClass(Client.class);
        verify(clientRepository).save(captor.capture());
        assertEquals("12345678A", captor.getValue().getDni());
    }
}
