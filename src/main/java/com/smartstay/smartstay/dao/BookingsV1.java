package com.smartstay.smartstay.dao;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Data
@Setter
@Getter
public class BookingsV1 {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String bookingId;
    private Date joiningDate;
    private Date leavingDate;
    private Double rentAmount;
    private Date createdAt;
    private Date updatedAt;
    private String createdBy;
    private String customerId;
    private String hostelId;
    private String currentStatus;
    private int floorId;
    private int roomId;
    private int bedId;
}
