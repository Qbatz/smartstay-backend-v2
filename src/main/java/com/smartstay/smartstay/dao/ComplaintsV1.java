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
public class  ComplaintsV1 {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer complaintId;


    private String customerId;
    private Integer complaintTypeId;
    private Integer floorId;
    private Integer roomId;
    private Integer bedId;
    private String status;
    private Date complaintDate;
    private String description;
    private Date createdAt;
    private Date updatedAt;
    private String createdBy;
    private String parentId;
    private String hostelId;
    private String assignee;
    private Date assignedDate;
    private Boolean isActive;
}
