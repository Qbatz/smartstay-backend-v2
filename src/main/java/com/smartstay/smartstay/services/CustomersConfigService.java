package com.smartstay.smartstay.services;

import com.smartstay.smartstay.Wrappers.recurring.CustomerListMapper;
import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.Customers;
import com.smartstay.smartstay.dao.CustomersConfig;
import com.smartstay.smartstay.dao.Users;
import com.smartstay.smartstay.payloads.recurring.UpdateRecurring;
import com.smartstay.smartstay.repositories.CustomersConfigRepository;
import com.smartstay.smartstay.responses.recurring.CustomersList;
import com.smartstay.smartstay.responses.recurring.RecurringList;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class CustomersConfigService {
    @Autowired
    private CustomersConfigRepository customersConfigRepository;
    @Autowired
    private UserHostelService userHostelService;
    @Autowired
    private RolesService rolesService;
    @Autowired
    private UsersService usersService;
    @Autowired
    private Authentication authentication;
    private CustomersService customersService;

    @Autowired
    private void setCustomersService(@Lazy CustomersService customersService) {
        this.customersService = customersService;
    }

    public ResponseEntity<?> getAllCustomers(String hostelId) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Users users = usersService.findUserByUserId(authentication.getName());
        if (users == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (!userHostelService.checkHostelAccess(users.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }
        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_CUSTOMERS, Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        List<CustomersConfig> customersConfigs = customersConfigRepository.findByHostelIdAndIsActiveTrue(hostelId);
        List<String> customerIds = customersConfigs
                .stream()
                .map(CustomersConfig::getCustomerId)
                .toList();
        List<Customers> listCustomers = customersService.getCustomerDetails(customerIds);
        if (listCustomers != null) {
            List<CustomersList> customersLists = listCustomers.stream()
                    .map(i -> new CustomerListMapper(customersConfigs).apply(i))
                    .toList();

            RecurringList recurringList = new RecurringList(hostelId, customersLists);

            return new ResponseEntity<>(recurringList, HttpStatus.OK);
        }
        RecurringList recurringList = new RecurringList(hostelId, null);
        return new ResponseEntity<>(recurringList, HttpStatus.OK);

    }

    public ResponseEntity<?> updateStatus(String hostelId, String customerId, UpdateRecurring updateRecurring) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Users users = usersService.findUserByUserId(authentication.getName());
        if (users == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        if (!userHostelService.checkHostelAccess(users.getUserId(), hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }
        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_CUSTOMERS, Utils.PERMISSION_UPDATE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        CustomersConfig customersConfig = customersConfigRepository.findByCustomerId(customerId);
        if (customersConfig == null) {
            return new ResponseEntity<>(Utils.INVALID_CUSTOMER_ID, HttpStatus.BAD_REQUEST);
        }
        if (updateRecurring == null) {
            customersConfig.setEnabled(false);
        }
        else if (updateRecurring.status() == null) {
            customersConfig.setEnabled(false);
        }
        else {
            customersConfig.setEnabled(updateRecurring.status());
        }

        customersConfigRepository.save(customersConfig);

        return new ResponseEntity<>(Utils.UPDATED, HttpStatus.OK);
    }

    public CustomersConfig addToConfiguration(String customerId, String hostelId, Date joiningDate) {

        CustomersConfig customersConfig = customersConfigRepository.findByCustomerId(customerId);
        if (customersConfig == null) {
            customersConfig = new CustomersConfig();
        }
        customersConfig.setCustomerId(customerId);
        customersConfig.setHostelId(hostelId);
        customersConfig.setCreatedBy(authentication.getName());
        customersConfig.setCreatedAt(joiningDate);
        customersConfig.setIsActive(true);
        customersConfig.setEnabled(true);

        return customersConfigRepository.save(customersConfig);
    }

    public List<CustomersConfig> getAllActiveAndEnabledRecurringCustomers(String hostelId) {
        List<CustomersConfig> listCustomers = customersConfigRepository.findActiveAndRecurringEnabledCustomersByHostelId(hostelId);
        if (listCustomers == null) {
            return new ArrayList<>();
        }
        return listCustomers;
    }
}
