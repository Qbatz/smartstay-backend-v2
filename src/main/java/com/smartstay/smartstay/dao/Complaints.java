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
public class Complaints {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String complaintId;

    private String customerId;
    private String complaintType;
    private int floorId;
    private int roomId;
    private int bedId;
    private Date complaintDate;
    private String description;
    private Date createdAt;
    private Date updatedAt;
    private String createdBy;
    private String parentId;
    private String hostelId;
    private Boolean isActive;
}
