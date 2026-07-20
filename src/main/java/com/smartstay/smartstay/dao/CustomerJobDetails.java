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
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CustomerJobDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long jobId;
    private String employmentStatus;
    private String organizationName;
    private String role;
    private String workLocation;
    private String shiftType;
    private String shiftStartTime;
    private String shiftEndTime;
    private String hostelId;
    private String customerId;
    private Boolean isDeleted;
    //from user type ENUM
    private String createdByUserType;
    //from user type ENUM
    private String updatedByUserType;
    private String createdBy;
    private Date createdAt;
    private String updatedBy;
    private Date updatedAt;
}
