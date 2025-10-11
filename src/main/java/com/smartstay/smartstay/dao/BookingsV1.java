package com.smartstay.smartstay.dao;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Entity
@Data
public class BookingsV1 {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String bookingId;
    private Date joiningDate;
    private Date leavingDate;
    private Date cancelDate;
    private Date expectedJoiningDate;
    private Date noticeDate;
    private Date bookingDate;
    private Double rentAmount;
    private Double advanceAmount;
    private Double bookingAmount;
    private Date createdAt;
    private Date updatedAt;
    private String createdBy;
    private String customerId;
    private String hostelId;
    private String currentStatus;
    private String updatedBy;
    private String reasonForLeaving;
    private String reasonForCancellation;
    private int floorId;
    private int roomId;
    private int bedId;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CustomersBedHistory> customerBedHistory;
}
