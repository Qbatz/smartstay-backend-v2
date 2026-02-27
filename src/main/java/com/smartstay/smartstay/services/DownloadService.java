package com.smartstay.smartstay.services;

import com.smartstay.smartstay.config.RestTemplateLoggingInterceptor;
import com.smartstay.smartstay.ennum.PaymentStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;

@Service
public class DownloadService {

    private final RestTemplate restTemplate;

    public DownloadService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        restTemplate.setInterceptors(Collections.singletonList(new RestTemplateLoggingInterceptor()));
    }

    public String downloadFromUrl(String url) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<Void> request =
                new HttpEntity<>(headers);

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
}
