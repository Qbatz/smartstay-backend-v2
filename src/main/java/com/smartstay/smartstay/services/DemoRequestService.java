package com.smartstay.smartstay.services;

import com.smartstay.smartstay.payloads.demo.DemoRequest;
import com.smartstay.smartstay.repositories.DemoRequestRepository;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class DemoRequestService {
    @Autowired
    private DemoRequestRepository demoRequestRepository;

    public ResponseEntity<?> requestDemo(DemoRequest demoRequest) {
        if (demoRequest == null) {
            return new ResponseEntity<>(Utils.PAYLOADS_REQUIRED, HttpStatus.BAD_REQUEST);
        }
        if (demoRequest.mobile() == null) {
            return new ResponseEntity<>(Utils.MOBILE_NO_REQUIRED, HttpStatus.BAD_REQUEST);
        }
        if (demoRequest.name() == null) {
            return new ResponseEntity<>(Utils.NAME_REQUIRED, HttpStatus.BAD_REQUEST);
        }
        if (demoRequest.city() == null) {
            return new ResponseEntity<>(Utils.CITY_REQUIRED, HttpStatus.BAD_REQUEST);
        }
        if (demoRequest.requestedDate() == null) {
            return new ResponseEntity<>(Utils.DATE_REQUIRED, HttpStatus.BAD_REQUEST);
        }

        Date requestedDate = Utils.stringToDate(demoRequest.requestedDate().replaceAll("/", "-"), Utils.USER_INPUT_DATE_FORMAT);
        Date time = null;
        String countryCode = null;
        if (demoRequest.countryCode() != null && !demoRequest.countryCode().isBlank()) {
            countryCode = demoRequest.countryCode();
        }

        com.smartstay.smartstay.dao.DemoRequest request = new com.smartstay.smartstay.dao.DemoRequest();
        request.setCity(demoRequest.city());
        request.setName(demoRequest.name());
        request.setEmailId(demoRequest.emailId());
        request.setContactNo(demoRequest.mobile());
        request.setCountryCode(countryCode);
        request.setOrganization(demoRequest.organization());
        request.setNoOfHostels(demoRequest.noOfProperties());
        request.setNoOfTenant(demoRequest.noOfTenants());
        request.setState(demoRequest.state());
        request.setCountry("India");
        request.setDemoRequestStatus(com.smartstay.smartstay.ennum.DemoRequest.REQUESTED.name());
        request.setIsDemoCompleted(false);

        demoRequestRepository.save(request);


        return new ResponseEntity<>(HttpStatus.OK);

    }
}
