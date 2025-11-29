package com.smartstay.smartstay.Wrappers.Notifications;

import com.smartstay.smartstay.dao.AdminNotifications;
import com.smartstay.smartstay.dao.Customers;
import com.smartstay.smartstay.dao.NotificationsV1;
import com.smartstay.smartstay.ennum.NotificationType;
import com.smartstay.smartstay.responses.Notifications.NotificationList;
import com.smartstay.smartstay.util.Utils;

import java.util.List;
import java.util.function.Function;

public class NotificationListMapper implements Function<AdminNotifications, NotificationList> {

    List<Customers> customers = null;

    public NotificationListMapper(List<Customers> customers) {
        this.customers = customers;
    }

    @Override
    public NotificationList apply(AdminNotifications notificationsV1) {

        StringBuilder fullName = new StringBuilder();

        int typeCode = 1;
        String notificationType = null;
        if (notificationsV1.getNotificationType().equalsIgnoreCase(NotificationType.AMENITY_REQUEST.name())) {
            notificationType = "Amenities";
            typeCode = 1;
        }
        else if (notificationsV1.getNotificationType().equalsIgnoreCase(NotificationType.CHANGE_BED.name())) {
            notificationType = "Bed Change";
            typeCode = 2;
        }
        else if (notificationsV1.getNotificationType().equalsIgnoreCase(NotificationType.CHECKOUT_REQUEST.name())) {
            notificationType = "Checkout";
            typeCode = 3;
        }
        else if (notificationsV1.getNotificationType().equalsIgnoreCase(NotificationType.COMPLAINT.name())) {
            notificationType = "Complaints";
            typeCode = 4;
        }
        else if (notificationsV1.getNotificationType().equalsIgnoreCase(NotificationType.MAINTENANCE.name())) {
            notificationType = "Maintenance";
            typeCode = 5;
        }
        else if (notificationsV1.getNotificationType().equalsIgnoreCase(NotificationType.CHECKOUT_MISSING.name())) {
            notificationType = "Missing checkout";
            typeCode = 6;
        }

        if (notificationsV1.getUserId() != null) {
            if (customers != null) {
                Customers cus = customers.stream()
                        .filter(i -> i.getCustomerId().equalsIgnoreCase(notificationsV1.getUserId()))
                        .findFirst()
                        .orElse(null);

                if (cus != null) {
                    fullName.append(cus.getFirstName());
                    if (cus.getLastName() != null && !cus.getLastName().equalsIgnoreCase("")) {
                        fullName.append(" ");
                        fullName.append(cus.getFirstName());
                    }
                }
            }
        }


        return new NotificationList(notificationsV1.getId(),
                notificationsV1.getTitle(),
                notificationsV1.getDescription(),
                notificationsV1.getUserId(),
                notificationType,
                typeCode,
                Utils.dateToString(notificationsV1.getCreatedAt()),
                notificationsV1.getSourceId(),
                fullName.toString(),
                notificationsV1.isRead());
    }
}
