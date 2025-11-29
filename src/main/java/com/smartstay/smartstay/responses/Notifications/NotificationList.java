package com.smartstay.smartstay.responses.Notifications;

public record NotificationList(Long notificationId,
                               String notificationTitle,
                               String notificationDescription,
                               String userId,
                               String type,
                               Integer typeCode,
                               String requestedAt,
                               String requestId,
                               String requestedUser,
                               boolean isRead) {
}
