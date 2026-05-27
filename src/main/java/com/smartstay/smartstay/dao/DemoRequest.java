package com.smartstay.smartstay.dao;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

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

    //from demo request source enum
    private String source;
    private String convertedToPlanCode;
    private Date demoDateFrom;
    private Date demoDateTo;
    //from demo type enum
    private String demoType;
    private String demoMeetLink;
    private String dropReason;

    private Date createdAt;

    @OneToMany(mappedBy = "demoRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DemoRequestComments> demoRequestComments;

    @OneToMany(mappedBy = "demoRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DemoRequestActivity> demoRequestActivities;
}
