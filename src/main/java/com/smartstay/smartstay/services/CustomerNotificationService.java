package com.smartstay.smartstay.services;

import com.smartstay.smartstay.config.Authentication;
import com.smartstay.smartstay.dao.ComplaintsV1;
import com.smartstay.smartstay.dao.CustomerNotifications;
import com.smartstay.smartstay.dao.Customers;
import com.smartstay.smartstay.dto.reminders.DueReminders;
import com.smartstay.smartstay.ennum.ComplaintStatus;
import com.smartstay.smartstay.ennum.NotificationType;
import com.smartstay.smartstay.ennum.UserType;
import com.smartstay.smartstay.repositories.CustomerNotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Service
public class CustomerNotificationService {
    @Autowired
    private Authentication authentication;
    @Autowired
    private CustomerNotificationRepository customerNotificationRepository;
    @Autowired
    private FCMNotificationService fcmNotificationService;

    public void addComplainUpdateStatus(ComplaintsV1 complaint, String name, String xuid, String newStatus, String complaintType) {
        String titleMessage = "Complaint updates";
        String description = complaintType + " has some update";

        if (newStatus.toLowerCase().equalsIgnoreCase(ComplaintStatus.RESOLVED.name().toLowerCase())) {
            description = "Your complaint for " + complaintType + " has been ressolved";
            titleMessage = "Updates on complaint " + complaintType;
        }
        else if (newStatus.toLowerCase().equalsIgnoreCase(ComplaintStatus.ASSIGNED.name().toLowerCase())) {
            titleMessage = "Updates on complaint " + complaintType;
            description = "Your complaint for " + complaintType + " has been assigned to " + name;
        }
        else if (newStatus.toLowerCase().equalsIgnoreCase(ComplaintStatus.IN_PROGRESS.name().toLowerCase())) {
            titleMessage = "Updates on complaint " + complaintType;
            description = "Your complaint for " + complaintType + " is moved for inprogress " + name;
        }
        else if (newStatus.toLowerCase().equalsIgnoreCase(ComplaintStatus.PENDING.name().toLowerCase())) {
            titleMessage = "Updates on complaint " + complaintType;
            description = "Your complaint has been put on pending by " + name;
        }

        CustomerNotifications customerNotifications = new CustomerNotifications();
        customerNotifications.setActive(true);
        customerNotifications.setNotificationType(NotificationType.COMPLAINT.name());
        customerNotifications.setUserId(complaint.getCustomerId());
        customerNotifications.setHostelId(complaint.getHostelId());
        customerNotifications.setDescription(description);
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

    public void notifyReminders(List<Customers> listCustomers, HashMap<String, DueReminders> customerReminders) {
        List<CustomerNotifications> listCustomerNotifications = listCustomers
                .stream()
                .map(i -> {
                    String description = "";
                    String titleMessage = "";
                    String sourceId = null;
                    DueReminders dueReminders = customerReminders.get(i.getCustomerId());
                    if (dueReminders != null) {
                        sourceId = dueReminders.customerId();
                        titleMessage = "Payment due reminders";
                        description = "Your payment is due soon. Please complete the payment before due.";
                    }
                    CustomerNotifications cn = new CustomerNotifications();
                    cn.setActive(true);
                    cn.setNotificationType(NotificationType.DUE_REMINDERS.name());
                    cn.setUserId(i.getCustomerId());
                    cn.setHostelId(i.getHostelId());
                    cn.setDescription(description);
                    cn.setSourceId(sourceId);
                    cn.setTitle(titleMessage);
                    cn.setUserType(UserType.TENANT.name());
                    cn.setCreatedAt(new Date());
                    cn.setActive(true);
                    cn.setDeleted(false);
                    cn.setRead(false);

                    return cn;
                })
                .toList();

        customerNotificationRepository.saveAll(listCustomerNotifications);

        fcmNotificationService.sendReminderNotification(listCustomers, customerReminders);


    }
}
