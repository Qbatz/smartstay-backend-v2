package com.smartstay.smartstay.services;

import com.smartstay.smartstay.config.RestTemplateLoggingInterceptor;
import com.smartstay.smartstay.dao.Credentials;
import com.smartstay.smartstay.ennum.PaymentStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;

@Service
public class DownloadService {

    private final RestTemplate restTemplate;
    @Autowired
    private CredentialServices credentialServices;

    public DownloadService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        restTemplate.setInterceptors(Collections.singletonList(new RestTemplateLoggingInterceptor()));
    }

    public String downloadFromUrl(String url) {
        Credentials credentials = credentialServices.findByServiceName(com.smartstay.smartstay.ennum.CredentialServices.SS_REPORTS.name());
        if (credentials == null) {
            return null;
        }
        if (credentials.getAuthToken() == null) {
            return  null;
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        String token = credentials.getAuthToken();

        headers.setBearerAuth(token);

        HttpEntity<Void> request =
                new HttpEntity<>(headers);
        //read tokem from credentials

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    request,
                    String.class
            );
            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            }
            else {
                return null;
            }
        }
        catch (HttpClientErrorException clientErrorException) {
            return null;
        }

    }
}
