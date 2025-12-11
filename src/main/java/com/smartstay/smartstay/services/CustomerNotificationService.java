package com.smartstay.smartstay.services;

import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.ComplaintsV1;
import com.smartstay.smartstay.dao.CustomerNotifications;
import com.smartstay.smartstay.ennum.ComplaintStatus;
import com.smartstay.smartstay.ennum.NotificationType;
import com.smartstay.smartstay.ennum.UserType;
import com.smartstay.smartstay.repositories.CustomerNotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class CustomerNotificationService {
    @Autowired
    private Authentication authentication;
    @Autowired
    private CustomerNotificationRepository customerNotificationRepository;
    @Autowired
    private FCMNotificationService fcmNotificationService;

    public void addComplainUpdateStatus(ComplaintsV1 complaint, String name, String xuid, String newStatus) {
        String titleMessage = "Complaint updates";

        if (newStatus.toLowerCase().equalsIgnoreCase(ComplaintStatus.RESOLVED.name().toLowerCase())) {
            titleMessage = "Your complaint has been ressolved";
        }
        else if (newStatus.toLowerCase().equalsIgnoreCase(ComplaintStatus.ASSIGNED.name().toLowerCase())) {
            titleMessage = "Your complaint has been assigned to " + name;
        }
        else if (newStatus.toLowerCase().equalsIgnoreCase(ComplaintStatus.PENDING.name().toLowerCase())) {
            titleMessage = "Your complaint has been put on pending by " + name;
        }

        CustomerNotifications customerNotifications = new CustomerNotifications();
        customerNotifications.setActive(true);
        customerNotifications.setNotificationType(NotificationType.COMPLAINT.name());
        customerNotifications.setUserId(complaint.getCustomerId());
        customerNotifications.setHostelId(complaint.getHostelId());
        customerNotifications.setDescription("");
        customerNotifications.setSourceId(String.valueOf(complaint.getComplaintId()));
        customerNotifications.setTitle(titleMessage);
        customerNotifications.setUserType(UserType.TENANT.name());
        customerNotifications.setCreatedAt(new Date());
        customerNotifications.setCreatedBy(authentication.getName());
        customerNotifications.setActive(true);
        customerNotifications.setDeleted(false);
        customerNotifications.setRead(false);

        customerNotificationRepository.save(customerNotifications);

        fcmNotificationService.updateCaseUpdate(complaint, name, xuid);

    }

    public void sendNotifications(String xuid, ComplaintsV1 complaintsV1, String comment, String name) {
        fcmNotificationService.addCommentNotification(xuid, complaintsV1, comment, name);
    }
}
