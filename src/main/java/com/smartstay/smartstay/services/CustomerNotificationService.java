package com.smartstay.smartstay.services;

import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.CustomerNotifications;
import com.smartstay.smartstay.ennum.NotificationType;
import com.smartstay.smartstay.repositories.CustomerNotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CustomerNotificationService {
    @Autowired
    private Authentication authentication;
    @Autowired
    private CustomerNotificationRepository customerNotificationRepository;

    public void addComplainUpdateStatus() {
        CustomerNotifications customerNotifications = new CustomerNotifications();
        customerNotifications.setActive(true);
        customerNotifications.setNotificationType(NotificationType.COMPLAINT.name());

    }
}
