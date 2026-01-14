package com.smartstay.smartstay.services;

import com.smartstay.smartstay.Wrappers.recurring.CustomerListMapper;
import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.Customers;
import com.smartstay.smartstay.dao.CustomersConfig;
import com.smartstay.smartstay.dao.InvoicesV1;
import com.smartstay.smartstay.dao.Users;
import com.smartstay.smartstay.dto.hostel.BillingDates;
import com.smartstay.smartstay.ennum.HostelEventModule;
import com.smartstay.smartstay.ennum.HostelEventType;
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
    @Autowired
    private InvoiceV1Service invoiceService;
    @Autowired
    private HostelService hostelService;
    private CustomersService customersService;

    @Autowired
    private HostelActivityLogService hostelActivityLogService;

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
        List<InvoicesV1> latestInvoicesList = invoiceService.findLatestInvoicesByCustomerIds(customerIds);
        List<Customers> listCustomers = customersService.getCustomerDetails(customerIds);
        BillingDates billingDates = hostelService.getNextBillingDates(hostelId);

        if (listCustomers != null) {
            List<CustomersList> customersLists = listCustomers.stream()
                    .map(i -> new CustomerListMapper(customersConfigs, latestInvoicesList, billingDates.currentBillStartDate()).apply(i))
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
        logActivity(hostelId, customerId, String.valueOf(customersConfig.getCustomerId()),
                HostelEventType.CUSTOMER_CONFIG_UPDATED, HostelEventModule.CUSTOMER_CONFIG,"Updated recurring status to " + customersConfig.getEnabled());

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

        CustomersConfig savedConfig = customersConfigRepository.save(customersConfig);
        logActivity(hostelId, customerId, String.valueOf(savedConfig.getCustomerId()),
                HostelEventType.CUSTOMER_CONFIG_CREATED, HostelEventModule.CUSTOMER_CONFIG,"Added to recurring customers configuration");
        return savedConfig;
    }

    public List<CustomersConfig> getAllActiveAndEnabledRecurringCustomers(String hostelId) {
        List<CustomersConfig> listCustomers = customersConfigRepository.findActiveAndRecurringEnabledCustomersByHostelId(hostelId);
        if (listCustomers == null) {
            return new ArrayList<>();
        }
        return listCustomers;
    }

    public void disableRecurring(String customerId) {
        CustomersConfig customerConfig = customersConfigRepository.findByCustomerId(customerId);
        if (customerConfig != null) {
            customerConfig.setEnabled(false);
            customerConfig.setIsActive(false);

            customersConfigRepository.save(customerConfig);
        }
    }

    public CustomersConfig findByCustomerIdAndHostelId(String customerId, String hostelId) {
        return customersConfigRepository.findByCustomerIdAndHostelId(customerId, hostelId);
    }

    private void logActivity(String hostelId, String parentId, String sourceId, HostelEventType eventType,
            HostelEventModule module,String description) {
        hostelActivityLogService.saveActivityLog(new Date(), hostelId, parentId, sourceId, eventType.getDisplayName(),
                module.getDisplayName(),description);
    }
}
