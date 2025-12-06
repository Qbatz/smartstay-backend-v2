package com.smartstay.smartstay.services;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class FCMNotificationService {

    @Autowired
    private FirebaseMessaging adminMessaging;
    @Autowired
    private FirebaseMessaging tenantMessaging;


    public void assignCase(String assignedTo) {

    }

    public ResponseEntity<?> sendTestMessage()  {
        Message message = Message.builder()
                .setToken("eutSAEryT3uMKc98cnVOfY:APA91bEhI2eWe1Iv5nbyqx4SulyV8nlVc86uNKYPBDCaAZhR8o5FnKfWggbcrftdmQ2Nqc8uHZXwhhUDOdGtMMXm9ZkQURTpOhKFsBq65OTA67sNgZUiUtY")
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
}
