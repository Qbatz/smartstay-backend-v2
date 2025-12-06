package com.smartstay.smartstay.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class FirebaseConfiguration {

    @Value("${ENVIRONMENT}")
    private String ENVIRONMENT;

    @Bean
    FirebaseMessaging configureFirebase() throws IOException {
        GoogleCredentials googleCredentials = null;

        if (!ENVIRONMENT.equalsIgnoreCase("PROD")) {
            googleCredentials = GoogleCredentials.fromStream(new ClassPathResource("smart-stay.json").getInputStream());
        }
        else {
            googleCredentials = GoogleCredentials.fromStream(new ClassPathResource("smart-stay.json").getInputStream());
        }

        FirebaseOptions firebaseOptions = FirebaseOptions.builder()
                .setCredentials(googleCredentials)
                .build();

        FirebaseApp app;

        try {
            app = FirebaseApp.getInstance("admin");
        } catch (IllegalStateException e) {
            app = FirebaseApp.initializeApp(firebaseOptions, "admin");
        }


        return FirebaseMessaging.getInstance(app);
    }
}
