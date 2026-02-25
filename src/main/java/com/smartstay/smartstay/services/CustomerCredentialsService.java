package com.smartstay.smartstay.services;

import com.smartstay.smartstay.dao.CustomerCredentials;
import com.smartstay.smartstay.repositories.CustomerCredentialsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class CustomerCredentialsService {

    @Autowired
    CustomerCredentialsRepository customerCredentialsRepository;

    public CustomerCredentials addCustomerCredentials(String mobile) {
        CustomerCredentials customerCredentials = customerCredentialsRepository.findByCustomerMobile(mobile);
        if (customerCredentials != null) {
            return customerCredentials;
        }
        CustomerCredentials newCustomerCredentils = new CustomerCredentials();
        newCustomerCredentils.setCustomerMobile(mobile);
        newCustomerCredentils.setCreatedAt(new Date());

        return customerCredentialsRepository.save(newCustomerCredentils);
    }

    public CustomerCredentials findByXuid(String xuid) {
        return customerCredentialsRepository.findByXuid(xuid);
    }

    public CustomerCredentials updateCustomerMobile(String mobile, String xuid) {
        CustomerCredentials ccs = customerCredentialsRepository.findByCustomerMobile(mobile);
        if (ccs == null) {
            CustomerCredentials ccs1 = customerCredentialsRepository.findByXuid(xuid);
            if (ccs1 != null) {
                ccs1.setCustomerMobile(mobile);
                return customerCredentialsRepository.save(ccs1);
            }
        }

        return null;
    }
}
