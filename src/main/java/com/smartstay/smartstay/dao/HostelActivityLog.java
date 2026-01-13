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
public class HostelActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long activityId;
    private String hostelId;
    private String parentId;
    private Date loggedAt;
    private Date createdAt;
    private String userId;
    private String sourceId;
    private String source;
    private String eventType;

}
