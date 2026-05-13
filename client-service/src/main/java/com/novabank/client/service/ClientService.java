package com.novabank.client.service;

import org.springframework.stereotype.Service;

import com.novabank.client.dto.ClientDTO;
import com.novabank.client.dto.CreateClient;
import com.novabank.client.exception.ClientNotFoundException;
import com.novabank.client.mapper.ClientMapper;
import com.novabank.client.repository.ClientRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ClientService {

    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public Flux<ClientDTO> listClients() {
        return clientRepository.findAll()
                .map(ClientMapper::toDTO);
    }

    public Mono<ClientDTO> getClient(Long id) {
        return clientRepository.findById(id)
                .map(ClientMapper::toDTO)
                .switchIfEmpty(Mono.error(new ClientNotFoundException("Client not found: " + id)));
    }

    public Mono<ClientDTO> getClientByDni(String dni) {
        return clientRepository.findByDni(dni)
                .map(ClientMapper::toDTO)
                .switchIfEmpty(Mono.error(new ClientNotFoundException("Client not found: " + dni)));
    }

    public Mono<ClientDTO> createClient(CreateClient dto) {
        //Validates, they are not blocking operations, so we can do them sequentially and short-circuit on the first error.
        if (dto.getFirstName() == null || dto.getFirstName().isBlank())
            throw new IllegalArgumentException("First name is needed");
        if (dto.getLastName() == null || dto.getLastName().isBlank())
            throw new IllegalArgumentException("Last name is needed");
        if (dto.getDni() == null || dto.getDni().isBlank())
            throw new IllegalArgumentException("DNI is needed");
        if (dto.getEmail() == null || dto.getEmail().isBlank())
            throw new IllegalArgumentException("Email is needed");
        if (dto.getPhone() == null || dto.getPhone().isBlank())
            throw new IllegalArgumentException("Phone is needed");

        // Checks for duplicates. We could do them in parallel, but it's simpler to do them sequentially and short-circuit on the first error.
        return clientRepository.existsByDni(dto.getDni())
                .flatMap(exists -> exists
                        ? Mono.<ClientDTO>error(
                                new IllegalArgumentException("A client with that DNI already exists: " + dto.getDni()))
                        : Mono.empty())
                .switchIfEmpty(clientRepository.existsByEmail(dto.getEmail())
                        .flatMap(exists -> exists
                                ? Mono.<ClientDTO>error(new IllegalArgumentException(
                                        "A client with that email already exists: " + dto.getEmail()))
                                : Mono.empty()))
                .switchIfEmpty(clientRepository.existsByPhone(dto.getPhone())
                        .flatMap(exists -> exists
                                ? Mono.<ClientDTO>error(new IllegalArgumentException(
                                        "A client with that telephone number already exists: " + dto.getPhone()))
                                : Mono.empty()))
                .switchIfEmpty(Mono.defer(() -> clientRepository.save(ClientMapper.toEntity(dto))
                        .map(ClientMapper::toDTO)));
    }
}
