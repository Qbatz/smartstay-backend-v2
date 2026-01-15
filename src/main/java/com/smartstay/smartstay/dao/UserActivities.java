package com.smartstay.smartstay.dao;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class UserActivities {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long activityId;
    private String description;
    private String userId;
    private Date loggedAt;
    private Date createdAt;
    private String parentId;
    //from Activity source enum
    private String source;
    private String sourceId;
//    from activity type enum
    private String activityType;
    private String hostelId;
}
