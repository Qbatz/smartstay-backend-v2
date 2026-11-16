package com.smartstay.smartstay.services;

import com.smartstay.smartstay.repositories.CustomerAmenityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CustomerAmenityService {
    @Autowired
    private CustomerAmenityRepository customerAmenityRepository;

}
