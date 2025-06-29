package com.smartstay.smartstay.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.smartstay.smartstay.Wrappers.ZohoSubscriptionMapper;
import com.smartstay.smartstay.config.RestTemplateLoggingInterceptor;
import com.smartstay.smartstay.dao.Credentials;
import com.smartstay.smartstay.dao.Subscription;
import com.smartstay.smartstay.payloads.ZohoSubscriptionRequest;
import com.smartstay.smartstay.repositories.CredentialsRepository;
import com.smartstay.smartstay.responses.AuthTokenResponse;
import com.smartstay.smartstay.responses.ZohoSubscription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;


@Service
public class SubscriptionService {
    @Autowired
    CredentialsRepository credentialsRepo;

    private final RestTemplate restTemplate;

    public SubscriptionService() {
        this.restTemplate = new RestTemplate();
        restTemplate.setInterceptors(Collections.singletonList(new RestTemplateLoggingInterceptor()));
    }

    public Subscription addSubscription(ZohoSubscriptionRequest request, int count) {

        Credentials credentials = credentialsRepo.findById("zoho").orElse(null);

        if (credentials != null) {
            String url = "https://www.zohoapis.in/billing/v1/subscriptions";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Zoho-oauthtoken " + credentials.getAuthToken());
            headers.set("X-com-zoho-subscriptions-organizationid", "60027939659");

            HttpEntity<ZohoSubscriptionRequest> entity = new HttpEntity<>(request, headers);

            try {
                ResponseEntity<ZohoSubscription> responseEntity =  restTemplate.exchange(url, HttpMethod.POST, entity, ZohoSubscription.class);
                if (responseEntity.getStatusCode() == HttpStatus.CREATED) {
                    return new ZohoSubscriptionMapper().apply(responseEntity.getBody());
                }
            }
            catch (HttpClientErrorException.Unauthorized unauthorized) {
                if (count < 3) {
                    return refreshZohoToken(request, credentials, count);
                }
                return null;
            }
        }
        return null;
    }

    private Subscription refreshZohoToken(ZohoSubscriptionRequest request, Credentials credentials, int count) {

        String url = "https://accounts.zoho.in/oauth/v2/token";

        MultiValueMap<String, String> formParams = new LinkedMultiValueMap<>();
        formParams.add("client_id", credentials.getClientId());
        formParams.add("client_secret", credentials.getSecretValue());
        formParams.add("refresh_token", credentials.getRefreshToken());
        formParams.add("redirect_uri", "https://finance.s3remotica.com/");
        formParams.add("grant_type", "refresh_token");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

//        HttpEntity<String> entity = new HttpEntity<>(headers);
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(formParams, headers);

        try {
            ResponseEntity<AuthTokenResponse> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, AuthTokenResponse.class);

            System.out.println("Raw response: " + response.getBody());


            if (response.getStatusCode() == HttpStatus.OK) {
                System.out.println("Access Token: " + response.getBody().getAccessToken());
                credentials.setAuthToken(response.getBody().getAccessToken());
//
                credentialsRepo.save(credentials);

                count = count + 1;
                return addSubscription(request, count);
            }
        }
        catch (HttpClientErrorException.BadRequest badRequest) {
            throw new RuntimeException("Something went wrong");
        }

        throw new RuntimeException("Unable to fetch client details");
    }
}
