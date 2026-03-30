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
public class DemoRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long requestId;
    private String name;
    private String emailId;
    private String contactNo;
    private String countryCode;
    private String organization;
    private Integer noOfHostels;
    private Integer noOfTenant;
    private String city;
    private String state;
    private String country;
    //From Demo request Enum
    private String demoRequestStatus;
    private Boolean isDemoCompleted;
    private Boolean isAssigned;
    private String assignedTo;
    private String assignedBy;
    private String presentedBy;
    private String comments;

    private Date bookedFor;
    private String requestedDate;
    private String requestedTime;
    private Date presentedAt;
}
