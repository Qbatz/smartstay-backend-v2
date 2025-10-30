package com.smartstay.smartstay.services;

import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.config.RestTemplateLoggingInterceptor;
import com.smartstay.smartstay.dao.Customers;
import com.smartstay.smartstay.dao.Users;
import com.smartstay.smartstay.dto.kyc.RequestKyc;
import com.smartstay.smartstay.ennum.KycStatus;
import com.smartstay.smartstay.payloads.ZohoSubscriptionRequest;
import com.smartstay.smartstay.repositories.KycRepository;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class KycServices {

    @Value("${KYC_BASE_URL}")
    private String KYC_BASE_URL;
    @Value("${KYC_REQUEST_END_POINT}")
    private String KYC_REQUEST_END_POINT;
    @Value("${KYC_USER_NAME}")
    private String KYC_USER_NAME;
    @Value("${KYC_PASSWORD}")
    private String KYC_PASSWORD;
    @Autowired
    private KycRepository kycRepository;
    @Autowired
    private Authentication authentication;
    @Autowired
    private CustomersService customersService;
    @Autowired
    private UsersService usersService;
    @Autowired
    private UserHostelService userHostelService;
    @Autowired
    private RolesService rolesService;

    private final RestTemplate restTemplate;

    public KycServices(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        restTemplate.setInterceptors(Collections.singletonList(new RestTemplateLoggingInterceptor()));
    }

    public ResponseEntity<?> requestKycService(String customerId) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Users users = usersService.findUserByUserId(authentication.getName());
        if (users == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Customers customers = customersService.getCustomerInformation(customerId);
        if (customers == null) {
            return new ResponseEntity<>(Utils.INVALID_CUSTOMER_ID, HttpStatus.BAD_REQUEST);
        }
        if (!userHostelService.checkHostelAccess(users.getUserId(), customers.getHostelId())) {
            return new ResponseEntity<>(Utils.RESTRICTED_HOSTEL_ACCESS, HttpStatus.BAD_REQUEST);
        }
        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_PAYING_GUEST, Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        if (customers.getKycStatus().equalsIgnoreCase(KycStatus.VERIFIED.name())) {
            return new ResponseEntity<>(Utils.CUSTOMER_VERIFIED_KYC, HttpStatus.BAD_REQUEST);
        }

        StringBuilder customerFullName = new StringBuilder();
        if (customers.getFirstName() != null) {
            customerFullName.append(customers.getFirstName());
        }
        if (customers.getLastName() != null && !customers.getLastName().trim().equalsIgnoreCase("")) {
            customerFullName.append(" ");
            customerFullName.append(customers.getLastName());
        }


        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("customer_identifier", customers.getMobile());
        requestBody.put("notify_customer", true);
        requestBody.put("customer_notification_mode", "SMS");
        requestBody.put("customer_name", customerFullName.toString());
        requestBody.put("template_name", "SMARTSTAY-WORKFLOW");
        requestBody.put("generate_access_token", true);

        requestKycApiCall(requestBody, 1);

        return null;
    }

    private RequestKyc requestKycApiCall(Map<String, Object> payloads, int code) {
        String kycUrl = KYC_BASE_URL + "/" + KYC_REQUEST_END_POINT;

        // Encode credentials to Base64
        String auth = KYC_USER_NAME + ":" + KYC_PASSWORD;
        String encodedAuth = Base64.getEncoder()
                .encodeToString(auth.getBytes(StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Basic "+ encodedAuth);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payloads, headers);

        try {
            ResponseEntity<RequestKyc> responseEntity = restTemplate.exchange(kycUrl, HttpMethod.POST, entity, RequestKyc.class);
            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                return responseEntity.getBody();
            }
            else {
                code = code + 1;
                if (code < 3) {
                    return requestKycApiCall(payloads, code);
                }
                return null;
            }
        }
        catch (Exception ae) {
            code = code + 1;
            if (code < 3) {
                return requestKycApiCall(payloads, code);
            }
            return null;
        }
    }
}
