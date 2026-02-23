package com.smartstay.smartstay.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class WhatsAppService {

    private final RestTemplate restTemplate;
    @Value("${WHATSAPP_PHONE_NUMBER_ID}")
    private String phoneNumberId;
    @Value("${WHATSAPP_ACCESS_TOKEN}")
    private String accessToken;
    @Value("${WHATSAPP_TEMPLATE_NAME}")
    private String templateName;
    @Value("${WHATSAPP_INVOICE_TEMPLATE_NAME:invoice_notification}")
    private String invoiceTemplateName;

    public WhatsAppService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void sendWelcomeMessage(String mobileNumber, String customerName) {
        String url = "https://graph.facebook.com/v17.0/" + phoneNumberId + "/messages";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + accessToken);

        String formattedMobile = mobileNumber.replaceAll("[^0-9]", "");
        if (formattedMobile.length() == 10) {
            formattedMobile = "91" + formattedMobile;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("messaging_product", "whatsapp");
        body.put("to", formattedMobile);
        body.put("type", "template");

        Map<String, Object> template = new HashMap<>();
        template.put("name", templateName);

        Map<String, String> language = new HashMap<>();
        language.put("code", "en");
        template.put("language", language);

        Map<String, Object> bodyComponent = new HashMap<>();
        bodyComponent.put("type", "body");

        Map<String, String> parameter = new HashMap<>();
        parameter.put("type", "text");
        parameter.put("text", customerName != null ? customerName : "Guest");

        bodyComponent.put("parameters", Collections.singletonList(parameter));
        template.put("components", Collections.singletonList(bodyComponent));

        body.put("template", template);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            restTemplate.postForEntity(url, entity, String.class);
        } catch (Exception e) {
            System.err.println("Failed to send WhatsApp message: " + e.getMessage());
        }
    }

    public void sendInvoiceNotification(String mobileNumber, String customerName, String invoiceUrl) {
        String url = "https://graph.facebook.com/v17.0/" + phoneNumberId + "/messages";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + accessToken);

        String formattedMobile = mobileNumber.replaceAll("[^0-9]", "");
        if (formattedMobile.length() == 10) {
            formattedMobile = "91" + formattedMobile;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("messaging_product", "whatsapp");
        body.put("to", formattedMobile);
        body.put("type", "template");

        Map<String, Object> template = new HashMap<>();
        template.put("name", invoiceTemplateName);

        Map<String, String> language = new HashMap<>();
        language.put("code", "en");
        template.put("language", language);


        Map<String, Object> bodyComponent = new HashMap<>();
        bodyComponent.put("type", "body");
        Map<String, String> bodyParameter = new HashMap<>();
        bodyParameter.put("type", "text");
        bodyParameter.put("text", customerName != null ? customerName : "Guest");
        bodyComponent.put("parameters", Collections.singletonList(bodyParameter));


        Map<String, Object> buttonComponent = new HashMap<>();
        buttonComponent.put("type", "button");
        buttonComponent.put("sub_type", "url");
        buttonComponent.put("index", "0");
        Map<String, String> buttonParameter = new HashMap<>();
        buttonParameter.put("type", "text");

        buttonParameter.put("text", invoiceUrl);
        buttonComponent.put("parameters", Collections.singletonList(buttonParameter));

        template.put("components", java.util.Arrays.asList(bodyComponent, buttonComponent));

        body.put("template", template);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            restTemplate.postForEntity(url, entity, String.class);
        } catch (Exception e) {
            System.err.println("Failed to send WhatsApp invoice message: " + e.getMessage());
        }
    }
}
