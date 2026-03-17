package com.smartstay.smartstay.responses.Notifications;

import java.util.List;

public record Notification(Integer unreadCount, List<NotificationList> listOfNotifications) {
}
