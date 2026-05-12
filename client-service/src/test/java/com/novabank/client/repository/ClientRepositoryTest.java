package com.novabank.client.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.test.context.ActiveProfiles;

import com.novabank.client.model.Client;

import reactor.test.StepVerifier;

@DataR2dbcTest
@ActiveProfiles("test")
class ClientRepositoryTest {

    @Autowired
    private ClientRepository clientRepository;

    @Test
    void saveAndFindByDni_shouldWork() {
        // DNI unique for this test run, to avoid conflicts with other tests or existing data.
        String dni = "TEST-" + java.util.UUID.randomUUID();

        Client client = new Client();
        client.setFirstName("Ana");
        client.setLastName("López");
        client.setDni(dni);
        client.setEmail("ana+" + dni + "@example.com");
        client.setPhone("600" + dni.substring(dni.length() - 6));

        StepVerifier.create(clientRepository.save(client)
                .then(clientRepository.findByDni(dni)))
                .assertNext(found -> assertEquals("Ana", found.getFirstName()))
                .verifyComplete();
    }
}
