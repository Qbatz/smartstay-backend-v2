package com.smartstay.smartstay.services;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.smartstay.smartstay.dao.ComplaintsV1;
import com.smartstay.smartstay.dao.CustomerCredentials;
import com.smartstay.smartstay.dao.CustomersConfig;
import com.smartstay.smartstay.ennum.NotificationMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class FCMNotificationService {
    @Autowired
    private FirebaseMessaging tenantMessaging;
    @Autowired
    private CustomerCredentialsService customerCredentialsService;


    public void assignCase(String assignedTo) {

    }

    public ResponseEntity<?> sendTestMessage()  {
        Message message = Message.builder()
                .setToken("camwl6RCTfKK4SenovTEX_:APA91bEosZS6EHAxZKTLwg_tKa09u7tCeN2lvBKLx_5WJd1zUWA332rehss9KY7SW1gCoW1mIxRNE-DxWRbCY9NTM1k4TRi0e1Rxa70OkY0tQHbyF0dNyNo")
                .putData("test", "testing.....")
                .putData("title", "test titile")
                .putData("body", "This is sample body data")
                .build();

        try {
            return new ResponseEntity<>(tenantMessaging.send(message), HttpStatus.OK);
        } catch (FirebaseMessagingException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateCaseUpdate(ComplaintsV1 complaint, String name, String xuid) {
        CustomerCredentials customerCredentials = customerCredentialsService.findByXuid(xuid);
        if (customerCredentials != null) {
            if (customerCredentials.getFcmToken() != null) {
                HashMap<String, String> payloads = new HashMap<>();
                payloads.put("title", "Your complaint has been assigned to " + name);
                payloads.put("type", NotificationMessage.COMPLAINT_ASSIGN.name());
                payloads.put("description", "Complaint for " + complaint.getDescription() + " is now assigned to " + name);

                Message message = Message.builder()
                        .setToken(customerCredentials.getFcmToken())
                        .putAllData(payloads)
                        .build();

                try {
                    tenantMessaging.send(message);
                } catch (FirebaseMessagingException e) {
                    throw new RuntimeException(e);
                }
            }
        }

    }
}
