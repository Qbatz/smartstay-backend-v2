package com.smartstay.smartstay.services;

import com.smartstay.smartstay.config.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TableConfigService {
    @Autowired
    private Authentication authentication;
}
