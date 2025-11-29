package com.smartstay.smartstay.services;

import com.smartstay.smartstay.Wrappers.Notifications.NotificationListMapper;
import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.*;
import com.smartstay.smartstay.ennum.UserType;
import com.smartstay.smartstay.repositories.NotificationV1Repository;
import com.smartstay.smartstay.responses.Notifications.Notification;
import com.smartstay.smartstay.responses.Notifications.NotificationList;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {
    @Autowired
    private Authentication authentication;
    @Autowired
    private HostelService hostelService;
    @Autowired
    private UsersService usersService;
    @Autowired
    private CustomersService customersService;
    @Autowired
    private NotificationV1Repository notificationV1Repository;
    public ResponseEntity<?> getAllNotifications(String hostelId) {
        if (!authentication.isAuthenticated()) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        Users users = usersService.findUserByUserId(authentication.getName());
        if (users == null) {
            return new ResponseEntity<>(Utils.UN_AUTHORIZED, HttpStatus.UNAUTHORIZED);
        }

        HostelV1 hostelV1 = hostelService.getHostelInfo(hostelId);
        if (hostelV1 == null) {
            return new ResponseEntity<>(Utils.INVALID_HOSTEL_ID, HttpStatus.BAD_REQUEST);
        }

        List<AdminNotifications> listNotifications = notificationV1Repository.findByHostelId(hostelId);
        List<AdminNotifications> unreadNotifications = listNotifications
                .stream()
                .filter(i -> !i.isRead())
                .toList();
        int unreadCount = unreadNotifications.size();
        List<String> requestedUsers = listNotifications
                .stream()
                .filter(i -> i.getUserType().equalsIgnoreCase(UserType.TENANT.name()))
                .map(AdminNotifications::getUserId)
                .toList();

        List<Customers> customers = customersService.getCustomerDetails(requestedUsers);

        List<NotificationList> notificationsList = listNotifications
                .stream()
                .map(i -> new NotificationListMapper(customers).apply(i))
                .toList();

        Notification notificationResponse = new Notification(unreadCount, notificationsList);

        return new ResponseEntity<>(notificationResponse, HttpStatus.OK);

    }
}
