package com.smartstay.smartstay.services;

import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.config.FilesConfig;
import com.smartstay.smartstay.config.RestTemplateLoggingInterceptor;
import com.smartstay.smartstay.config.UploadFileToS3;
import com.smartstay.smartstay.dao.*;
import com.smartstay.smartstay.dao.KycAddressDetails;
import com.smartstay.smartstay.dto.documents.UploadFiles;
import com.smartstay.smartstay.dto.kyc.*;
import com.smartstay.smartstay.ennum.CustomerStatus;
import com.smartstay.smartstay.ennum.FileFormat;
import com.smartstay.smartstay.ennum.KycDocumentType;
import com.smartstay.smartstay.ennum.KycStatus;
import com.smartstay.smartstay.repositories.KycRepository;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;

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
    private UploadFileToS3 uploadFileToS3;
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
    @Autowired
    private CustomerNotificationService customerNotificationService;
    @Autowired
    private SubscriptionService subscriptionService;

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
        if (!subscriptionService.validateSubscription(customers.getHostelId())) {
            return new ResponseEntity<>(Utils.SUBSCRIPTION_EXPIRED, HttpStatus.FORBIDDEN);
        }
        if (!rolesService.checkPermission(users.getRoleId(), Utils.MODULE_ID_PAYING_GUEST, Utils.PERMISSION_READ)) {
            return new ResponseEntity<>(Utils.ACCESS_RESTRICTED, HttpStatus.FORBIDDEN);
        }
        if (customers.getCurrentStatus().equalsIgnoreCase(CustomerStatus.VACATED.name())) {
            return new ResponseEntity<>(Utils.CANNOT_REQUEST_VACATED_TENANT, HttpStatus.BAD_REQUEST);
        }
        if (customers.getCurrentStatus().equalsIgnoreCase(CustomerStatus.BOOKED.name())) {
            return new ResponseEntity<>(Utils.CANNOT_REQUEST_BOOKING_TENANT, HttpStatus.BAD_REQUEST);
        }
        if (customers.getCurrentStatus().equalsIgnoreCase(CustomerStatus.INACTIVE.name())) {
            return new ResponseEntity<>(Utils.CANNOT_REQUEST_INACTIVE_TENANT, HttpStatus.BAD_REQUEST);
        }
        if (customers.getCurrentStatus().equalsIgnoreCase(CustomerStatus.DRAFT.name())) {
            return new ResponseEntity<>(Utils.CANNOT_REQUEST_DRAFTED_TENANT, HttpStatus.BAD_REQUEST);
        }
        if (customers.getCurrentStatus().equalsIgnoreCase(CustomerStatus.CANCELLED_BOOKING.name())) {
            return new ResponseEntity<>(Utils.CANNOT_REQUEST_CANCELLED_TENANT, HttpStatus.BAD_REQUEST);
        }

        KycDetails kycDetails = customers.getKycDetails();
        if (kycDetails != null) {
            if (kycDetails.getCurrentStatus().equalsIgnoreCase(KycStatus.VERIFIED.name())) {
                return new ResponseEntity<>(Utils.CUSTOMER_VERIFIED_KYC, HttpStatus.BAD_REQUEST);
            }
            if (kycDetails.getCurrentStatus().equalsIgnoreCase(KycStatus.WAITING_FOR_APPROVAL.name())) {
                return new ResponseEntity<>(Utils.KYC_VERIFICATION_ALREADY_REQUESTED, HttpStatus.BAD_REQUEST);
            }
            if (kycDetails.getCurrentStatus().equalsIgnoreCase(KycStatus.REQUESTED.name())) {
                return new ResponseEntity<>(Utils.KYC_VERIFICATION_ALREADY_REQUESTED, HttpStatus.BAD_REQUEST);
            }
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

        RequestKyc requestKyc = requestKycApiCall(requestBody, 1);

        if (requestKyc != null) {
            RequestKyc.AccessToken kycAccessToken = requestKyc.getAccessToken();
            if (kycDetails == null) {
                kycDetails = new KycDetails();
                kycDetails.setCustomers(customers);
            }
            kycDetails.setCurrentStatus(KycStatus.REQUESTED.name());
            kycDetails.setTransactionId(requestKyc.getTransactionId());
            kycDetails.setTemplateId(requestKyc.getTemplateId());
            kycDetails.setReferenceId(requestKyc.getReferenceId());
            kycDetails.setCreatedAt(new Date());
            kycDetails.setCreatedBy(authentication.getName());

            if (kycAccessToken != null) {
                kycDetails.setEntityId(kycAccessToken.getEntityId());
                kycDetails.setAccessTokenId(kycAccessToken.getId());
                kycDetails.setExpireAt(Utils.stringToDateTime(kycAccessToken.getValidTill()));
            }

            kycRepository.save(kycDetails);

            customerNotificationService.sendKycNotification(customers, kycDetails, users, customers.getHostelId());

            return new ResponseEntity<>(HttpStatus.OK);
        }


        return new ResponseEntity<>(Utils.TRY_AGAIN, HttpStatus.BAD_REQUEST);
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

    public KycDetails verifyStatus(Customers customers) {
        if (!authentication.isAuthenticated()) {
            return null;
        }
        KycDetails kycDetails = customers.getKycDetails();
        if (kycDetails == null) {
            return null;
        }

        String endPoint = "client/kyc/v2/" +kycDetails.getEntityId() + "/response";
        String verifyKycUrl = KYC_BASE_URL + "/" + endPoint;
        // Encode credentials to Base64
        String auth = KYC_USER_NAME + ":" + KYC_PASSWORD;
        String encodedAuth = Base64.getEncoder()
                .encodeToString(auth.getBytes(StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Basic "+ encodedAuth);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(headers);

        ResponseEntity<VerifyKyc> responseEntity = restTemplate.exchange(verifyKycUrl, HttpMethod.POST, entity, VerifyKyc.class);
        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            VerifyKyc verifyKycResponse = responseEntity.getBody();
            if (verifyKycResponse != null) {
                if (verifyKycResponse.getStatus().equalsIgnoreCase("approved")) {
                    if (!kycDetails.getCurrentStatus().equalsIgnoreCase(KycStatus.VERIFIED.name())) {
                        if (verifyKycResponse.getStatus().equalsIgnoreCase("APPROVED")) {
                            kycDetails.setCurrentStatus(KycStatus.VERIFIED.name());
                        }
                    }

                    List<VerifyKycActions> listKycActions = verifyKycResponse.getActions();
                    if (listKycActions != null && !listKycActions.isEmpty()) {
                        VerifyKycActions kycActions = listKycActions.get(0);
                        kycDetails.setCompletedAt(Utils.stringToDateTime(kycActions.getCompletedAt()));
                        if (kycActions.getExecutionRequestId() != null) {
                            UploadFiles uploadFiles = getKycPdfDocument(customers.getCustomerId(), kycActions.getExecutionRequestId());

                            if (uploadFiles != null) {
                                kycDetails.setKycDocumentType(FileFormat.PDF.name());
                                kycDetails.setKycDocument(uploadFiles.fileName());
                            }
                        }
                        VerifyKycActionDetails verifyKycActionDetails = kycActions.getDetails();
                        if (verifyKycActionDetails != null) {

                            AadhaarDetails aadhaarDetails = verifyKycActionDetails.getAadhaarDetails();
                            if (aadhaarDetails != null ) {
                                kycDetails.setAadhaarNumber(aadhaarDetails.getIdNumber());
                                kycDetails.setDocumentType(KycDocumentType.AADHAAR.name());
                                kycDetails.setNameInDocument(aadhaarDetails.getName());
                                kycDetails.setDateOfBirth(aadhaarDetails.getDateOfBirth());
                                kycDetails.setPermanentAddress(aadhaarDetails.getPermanentAddressString());
                                kycDetails.setUpdatedAt(new Date());

                                if (aadhaarDetails.getGender().equalsIgnoreCase("F")) {
                                    kycDetails.setGender("Female");
                                }
                                else if (aadhaarDetails.getGender().equalsIgnoreCase("M")) {
                                    kycDetails.setGender("Male");
                                }
                                else {
                                    kycDetails.setGender("Others");
                                }

                                KycAddressDetails addressDetails = kycDetails.getAddressDetails();
                                if (addressDetails == null) {
                                    addressDetails = new KycAddressDetails();

                                }
                                com.smartstay.smartstay.dto.kyc.KycAddressDetails responseAddress = aadhaarDetails.getCurrentAddressDetails();
                                if (responseAddress != null) {
                                    addressDetails.setCurrentCity(responseAddress.getDistrictOrCity());
                                    addressDetails.setCurrentAddress(responseAddress.getAddress());
                                    addressDetails.setCurrentLocality(responseAddress.getLocalityOrPostOffice());
                                    addressDetails.setCurrentPincode(responseAddress.getPincode());
                                    addressDetails.setCurrentState(responseAddress.getState());
                                }
                                com.smartstay.smartstay.dto.kyc.KycAddressDetails responsePermanentAddress = aadhaarDetails.getPermanentAddress();
                                if (responsePermanentAddress != null) {
                                    addressDetails.setPermanentCity(responsePermanentAddress.getDistrictOrCity());
                                    addressDetails.setPermanentAddress(responsePermanentAddress.getAddress());
                                    addressDetails.setPermanentLocality(responsePermanentAddress.getLocalityOrPostOffice());
                                    addressDetails.setPermanentPincode(responsePermanentAddress.getPincode());
                                    addressDetails.setPermanentState(responsePermanentAddress.getState());
                                }
                                addressDetails.setKycDetails(kycDetails);
                                kycDetails.setAddressDetails(addressDetails);



                                String aadhaarImage = uploadFileToS3.uploadFileToS3(FilesConfig.base64ToImage(customers.getCustomerId(), aadhaarDetails.getImage()), "kyc-pic");
                                kycDetails.setIdPic(aadhaarImage);

                            }
                        }

                    }

                    kycRepository.save(kycDetails);
                }
            }

        }

        return kycDetails;
    }

    private UploadFiles getKycPdfDocument(String customerId, String executionRequestId) {
        String endPoint = "client/kyc/v2/media/" + executionRequestId ;
        String verifyKycUrl = KYC_BASE_URL + "/" + endPoint;

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(verifyKycUrl)
                .queryParam("doc_type", "AADHAAR")
                .queryParam("base64", true);


        String auth = KYC_USER_NAME + ":" + KYC_PASSWORD;
        String encodedAuth = Base64.getEncoder()
                .encodeToString(auth.getBytes(StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Basic "+ encodedAuth);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(headers);

        ResponseEntity<AadharMediaResponse> responseEntity = restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, AadharMediaResponse.class);
        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            AadharMediaResponse mediaResponse = responseEntity.getBody();
            if (mediaResponse != null && mediaResponse.getFile() != null) {
                byte[] pdfBytes = Base64.getDecoder().decode(mediaResponse.getFile().getBytes());

                File aadhaarPdf = FilesConfig.writePdf(pdfBytes, customerId);
                UploadFiles uploadFiles = uploadFileToS3.uploadCustomerFiles(aadhaarPdf, "kyc-docs");
                return uploadFiles;
            }


        }

        return null;
    }

    public List<KycDetails> getAllRequestWithRequestedState() {
        List<KycDetails> listKyc = kycRepository.findAllRequested();
        if (listKyc == null) {
            listKyc = new ArrayList<>();
        }
        return listKyc;
    }

    public void verifyKycStatusAndUpdate(KycDetails item) {
        KycDetails kycDetails = item;
        if (kycDetails == null) {
            return;
        }

        String endPoint = "client/kyc/v2/" + kycDetails.getEntityId() + "/response";
        String verifyKycUrl = KYC_BASE_URL + "/" + endPoint;
        // Encode credentials to Base64
        String auth = KYC_USER_NAME + ":" + KYC_PASSWORD;
        String encodedAuth = Base64.getEncoder()
                .encodeToString(auth.getBytes(StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Basic " + encodedAuth);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(headers);

        ResponseEntity<VerifyKyc> responseEntity = restTemplate.exchange(verifyKycUrl, HttpMethod.POST, entity, VerifyKyc.class);
        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            VerifyKyc verifyKycResponse = responseEntity.getBody();
            if (verifyKycResponse != null) {
                if (verifyKycResponse.getStatus().equalsIgnoreCase("approved")) {
                    if (!kycDetails.getCurrentStatus().equalsIgnoreCase(KycStatus.VERIFIED.name())) {
                        if (verifyKycResponse.getStatus().equalsIgnoreCase("APPROVED")) {
                            kycDetails.setCurrentStatus(KycStatus.VERIFIED.name());
                        }
                    }

                    List<VerifyKycActions> listKycActions = verifyKycResponse.getActions();
                    if (listKycActions != null && !listKycActions.isEmpty()) {
                        VerifyKycActions kycActions = listKycActions.get(0);
                        kycDetails.setCompletedAt(Utils.stringToDateTime(kycActions.getCompletedAt()));
                        if (kycActions.getExecutionRequestId() != null) {
                            Customers customers = item.getCustomers();
                            UploadFiles uploadFiles = getKycPdfDocument(customers.getCustomerId(), kycActions.getExecutionRequestId());

                            if (uploadFiles != null) {
                                kycDetails.setKycDocumentType(FileFormat.PDF.name());
                                kycDetails.setKycDocument(uploadFiles.fileName());
                            }
                        }
                        VerifyKycActionDetails verifyKycActionDetails = kycActions.getDetails();
                        if (verifyKycActionDetails != null) {

                            AadhaarDetails aadhaarDetails = verifyKycActionDetails.getAadhaarDetails();
                            if (aadhaarDetails != null) {
                                kycDetails.setAadhaarNumber(aadhaarDetails.getIdNumber());
                                kycDetails.setDocumentType(KycDocumentType.AADHAAR.name());
                                kycDetails.setNameInDocument(aadhaarDetails.getName());
                                kycDetails.setDateOfBirth(aadhaarDetails.getDateOfBirth());
                                kycDetails.setPermanentAddress(aadhaarDetails.getPermanentAddressString());
                                kycDetails.setUpdatedAt(new Date());

                                if (aadhaarDetails.getGender().equalsIgnoreCase("F")) {
                                    kycDetails.setGender("Female");
                                } else if (aadhaarDetails.getGender().equalsIgnoreCase("M")) {
                                    kycDetails.setGender("Male");
                                } else {
                                    kycDetails.setGender("Others");
                                }

                                KycAddressDetails addressDetails = kycDetails.getAddressDetails();
                                if (addressDetails == null) {
                                    addressDetails = new KycAddressDetails();

                                }
                                com.smartstay.smartstay.dto.kyc.KycAddressDetails responseAddress = aadhaarDetails.getCurrentAddressDetails();
                                if (responseAddress != null) {
                                    addressDetails.setCurrentCity(responseAddress.getDistrictOrCity());
                                    addressDetails.setCurrentAddress(responseAddress.getAddress());
                                    addressDetails.setCurrentLocality(responseAddress.getLocalityOrPostOffice());
                                    addressDetails.setCurrentPincode(responseAddress.getPincode());
                                    addressDetails.setCurrentState(responseAddress.getState());
                                }
                                com.smartstay.smartstay.dto.kyc.KycAddressDetails responsePermanentAddress = aadhaarDetails.getPermanentAddress();
                                if (responsePermanentAddress != null) {
                                    addressDetails.setPermanentCity(responsePermanentAddress.getDistrictOrCity());
                                    addressDetails.setPermanentAddress(responsePermanentAddress.getAddress());
                                    addressDetails.setPermanentLocality(responsePermanentAddress.getLocalityOrPostOffice());
                                    addressDetails.setPermanentPincode(responsePermanentAddress.getPincode());
                                    addressDetails.setPermanentState(responsePermanentAddress.getState());
                                }
                                addressDetails.setKycDetails(kycDetails);
                                kycDetails.setAddressDetails(addressDetails);


                                Customers customers = item.getCustomers();

                                String aadhaarImage = uploadFileToS3.uploadFileToS3(FilesConfig.base64ToImage(customers.getCustomerId(), aadhaarDetails.getImage()), "kyc-pic");
                                kycDetails.setIdPic(aadhaarImage);

                            }
                        }

                    }

                    kycRepository.save(kycDetails);
                }
            }
        }
    }
}
