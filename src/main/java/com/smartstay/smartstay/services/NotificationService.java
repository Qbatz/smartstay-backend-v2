package com.smartstay.smartstay.services;

import com.smartstay.smartstay.Wrappers.Notifications.NotificationListMapper;
import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.*;
import com.smartstay.smartstay.ennum.NotificationType;
import com.smartstay.smartstay.ennum.UserType;
import com.smartstay.smartstay.repositories.NotificationV1Repository;
import com.smartstay.smartstay.responses.Notifications.Notification;
import com.smartstay.smartstay.responses.Notifications.NotificationList;
import com.smartstay.smartstay.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class NotificationService {
    @Autowired
    private Authentication authentication;
    @Autowired
    private UsersService usersService;
    @Autowired
    private NotificationV1Repository notificationV1Repository;

    private HostelService hostelService;
    private CustomersService customersService;

    @Autowired
    public void setHostelService(@Lazy HostelService hostelService) {
        this.hostelService = hostelService;
    }
    @Autowired
    public void setCustomersService(@Lazy CustomersService customersService) {
        this.customersService = customersService;
    }

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

    public void addAdminNotificationsForRecurringInvoice(String hostelId) {
        AdminNotifications adminNotifications = new AdminNotifications();
        adminNotifications.setNotificationType(NotificationType.RECURRING_INVOICE.name());
        adminNotifications.setUserId(null);
        adminNotifications.setHostelId(hostelId);
        adminNotifications.setSourceId(null);
        adminNotifications.setDescription("New rental invoice is generated successfully. ");
        adminNotifications.setTitle("Recurring invoice has beed generated.");
        adminNotifications.setUserType(UserType.ALL_EXCEPT_TENANT.name());
        adminNotifications.setCreatedAt(new Date());
        adminNotifications.setActive(true);
        adminNotifications.setRead(false);

        notificationV1Repository.save(adminNotifications);
    }

    public int getUnreadNotificationCount(String hostelId) {
        List<AdminNotifications> listUnreadNotifications = notificationV1Repository.findUnReadNotifications(hostelId);
        if (listUnreadNotifications != null) {
            return listUnreadNotifications.size();
        }
        return 0;
    }
}
