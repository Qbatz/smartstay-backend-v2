package com.smartstay.smartstay.dao;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerNotifications {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    //Notification type
    private String notificationType;
    private String userId;
    private String hostelId;
    private String description;
    private String sourceId;
    private String title;
    private String userType;
    private Date createdAt;
    private Date updatedAt;
    private boolean isActive;
    private boolean isRead;
    private String createdBy;
    private boolean isDeleted;
}
