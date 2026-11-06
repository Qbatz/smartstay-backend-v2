package com.smartstay.smartstay.services;

import com.smartstay.smartstay.dao.Credentials;
import com.smartstay.smartstay.repositories.CredentialsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CredentialServices {
    @Autowired
    private CredentialsRepository credentialsRepository;

    public Credentials findByServiceName(String serviceName) {
        return credentialsRepository.findByService(serviceName);
    }
}
