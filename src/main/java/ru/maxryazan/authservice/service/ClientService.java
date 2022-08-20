package ru.maxryazan.authservice.service;

import org.springframework.stereotype.Service;
import ru.maxryazan.authservice.model.Client;
import ru.maxryazan.authservice.repository.ClientRepository;

import java.util.Optional;

@Service
public class ClientService {
    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public Optional<Client> findByPhoneNumber(String phoneNumber){
        return clientRepository.findByPhoneNumber(phoneNumber);
    }
}
