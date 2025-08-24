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
public class ComplaintTypeV1 {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer complaintTypeId;
    private Date createdAt;
    private Date updatedAt;
    private String createdBy;
    private String complaintTypeName;
    private String parentId;
    private String hostelId;
    private Boolean isActive;
}
