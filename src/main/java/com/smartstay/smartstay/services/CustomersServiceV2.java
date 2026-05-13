package com.smartstay.smartstay.services;

import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.CustomerCredentials;
import com.smartstay.smartstay.dao.Customers;
import com.smartstay.smartstay.dao.Users;
import com.smartstay.smartstay.ennum.CustomerBedStatus;
import com.smartstay.smartstay.ennum.CustomerStatus;
import com.smartstay.smartstay.ennum.KycStatus;
import com.smartstay.smartstay.ennum.ModuleId;
import com.smartstay.smartstay.payloads.customer.SaveDraftCustomerRequest;
import com.smartstay.smartstay.repositories.CustomersRepository;
import com.smartstay.smartstay.responses.customer.CustomerSearchResponse;
import com.smartstay.smartstay.util.NameUtils;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Collections;

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

    @Autowired
    private RolesService rolesService;

    @Autowired
    private CustomerCredentialsService ccs;

    @Autowired
    private SubscriptionService subscriptionService;

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

    public ResponseEntity<?> saveDraft(String hostelId, SaveDraftCustomerRequest payloads) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        String loginId = authentication.getName();
        Users user = userService.findUserByUserId(loginId);
        if (user == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        if (!rolesService.checkPermission(user.getRoleId(), ModuleId.WALK_IN.getId(), Utils.PERMISSION_WRITE)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }

        if (!userHostelService.checkHostelAccess(loginId, hostelId)) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.FORBIDDEN);
        }

        if (!subscriptionService.validateSubscription(hostelId)) {
            return new ResponseEntity<>(Utils.SUBSCRIPTION_EXPIRED, HttpStatus.FORBIDDEN);
        }

        String mobileStatus = "";
        String emailStatus = "";

        if (customersRepository.existsByMobileAndHostelIdAndStatusesNotIn(payloads.mobile(), hostelId, List.of(CustomerStatus.VACATED.name()))) {
            mobileStatus = Utils.MOBILE_NO_EXISTS;
        }

        if (Utils.checkNullOrEmpty(payloads.emailId())) {
            if (customersRepository.existsByEmailIdAndHostelIdAndStatusesNotIn(payloads.emailId(), hostelId, List.of(CustomerStatus.VACATED.name()))) {
                emailStatus = Utils.EMAIL_ID_EXISTS;
            }
        }

        if (!mobileStatus.isEmpty() || !emailStatus.isEmpty()) {
            return new ResponseEntity<>(Map.of(
                    "mobileStatus", mobileStatus,
                    "emailStatus", emailStatus,
                    "message", "Validation failed"
            ), HttpStatus.BAD_REQUEST);
        }

        Customers customers = new Customers();
        customers.setFirstName(payloads.firstName());
        customers.setLastName(payloads.lastName());
        customers.setCountry(1L);
        customers.setMobile(payloads.mobile());
        customers.setEmailId(payloads.emailId());
        customers.setCustomerBedStatus(CustomerBedStatus.BED_NOT_ASSIGNED.name());
        customers.setCurrentStatus(CustomerStatus.DRAFT.name());
        customers.setHostelId(hostelId);
        customers.setCreatedBy(loginId);
        customers.setCreatedAt(new Date());
        customers.setKycStatus(KycStatus.NOT_AVAILABLE.name());
        customers.setProfilePic(null);

        if (Utils.checkNullOrEmpty(payloads.joiningDate())) {
            Date joiningDate = Utils.stringToDate(payloads.joiningDate().replace("/", "-"), Utils.USER_INPUT_DATE_FORMAT);
            customers.setExpJoiningDate(joiningDate);
        }

        CustomerCredentials customerCredentials = ccs.addCustomerCredentials(payloads.mobile());
        if (customerCredentials != null) {
            customers.setXuid(customerCredentials.getXuid());
        }

        Customers savedCustomer = customersRepository.save(customers);
        return new ResponseEntity<>(Map.of(
                "message", Utils.CREATED,
                "customerId", savedCustomer.getCustomerId(),
                "currentStatus", savedCustomer.getCurrentStatus()
        ), HttpStatus.CREATED);
    }
}
