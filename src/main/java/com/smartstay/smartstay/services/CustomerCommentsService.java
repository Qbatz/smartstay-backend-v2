package com.smartstay.smartstay.services;

import com.smartstay.smartstay.repositories.CustomersCommentsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CustomerCommentsService {

    @Autowired
    private CustomersCommentsRepository customersCommentsRepository;
}
