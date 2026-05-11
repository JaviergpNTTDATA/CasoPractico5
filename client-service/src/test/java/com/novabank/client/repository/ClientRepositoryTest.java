package com.novabank.client.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.test.context.ActiveProfiles;

import com.novabank.client.model.Client;

import reactor.test.StepVerifier;
@Disabled
@DataR2dbcTest
@ActiveProfiles("test")
class ClientRepositoryTest {

    private final ClientRepository clientRepository;

    ClientRepositoryTest(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Test
    void saveAndFindByDni_shouldWork() {
        Client client = new Client();
        client.setFirstName("Ana");
        client.setLastName("López");
        client.setDni("87654321B");
        client.setEmail("ana@example.com");
        client.setPhone("600111111");

        StepVerifier.create(clientRepository.save(client)
                .then(clientRepository.findByDni("87654321B")))
                .assertNext(found -> assertEquals("Ana", found.getFirstName()))
                .verifyComplete();
    }
}
