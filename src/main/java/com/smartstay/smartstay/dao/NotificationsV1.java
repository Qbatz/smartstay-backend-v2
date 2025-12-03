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
public class NotificationsV1 {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    //Notification type enum
    private String notificationType;
    //requested user id
    private String userId;
    private String hostelId;
    private String description;
    //amenity id or complience id, bed id optional
    private String sourceId;
    private String title;
    //Tenant or admin
    private String userType;
    private Date createdAt;
    private Date updatedAt;
    private boolean isActive;
    private boolean isRead;
    private String createdBy;
    private boolean isDeleted;
}
