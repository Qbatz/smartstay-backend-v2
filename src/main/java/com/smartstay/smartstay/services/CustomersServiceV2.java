package com.smartstay.smartstay.services;

import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.Customers;
import com.smartstay.smartstay.dao.Users;
import com.smartstay.smartstay.repositories.CustomersRepository;
import com.smartstay.smartstay.responses.customer.CustomerSearchResponse;
import com.smartstay.smartstay.util.NameUtils;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class CustomersServiceV2 {

    @Autowired
    private Authentication authentication;

    @Autowired
    private UsersService userService;

    @Autowired
    private UserHostelService userHostelService;

    @Autowired
    private CustomersRepository customersRepository;

    public ResponseEntity<?> searchCustomersByMobile(String hostelId, String search) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Users users = userService.findUserByUserId(authentication.getName());
        if (users == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (!userHostelService.checkHostelAccess(users.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }
        if (search == null || search.trim().length() < 4) {
            return new ResponseEntity<>("Minimum 4 digits required for search", HttpStatus.BAD_REQUEST);
        }

        List<Customers> matchedCustomers = customersRepository.searchByMobileAndHostelId(hostelId, search.trim());
        if (matchedCustomers == null || matchedCustomers.isEmpty()) {
            return new ResponseEntity<>(Collections.emptyList(), HttpStatus.OK);
        }

        List<CustomerSearchResponse> result = matchedCustomers
                .stream()
                .map(c -> new CustomerSearchResponse(
                        c.getCustomerId(),
                        NameUtils.getFullName(c.getFirstName(), c.getLastName()),
                        c.getFirstName(),
                        c.getLastName(),
                        c.getProfilePic(),
                        NameUtils.getInitials(c.getFirstName(), c.getLastName()),
                        "+91 " + c.getMobile(),
                        c.getEmailId()))
                .toList();

        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
