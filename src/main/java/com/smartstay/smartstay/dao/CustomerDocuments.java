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
public class CustomerDocuments {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long documentId;
    //should be frmo DocumentType enum
    private String documentType;
    private String documentUrl;
    //from File format enum
    private String documentFileType;
    private String customerId;
    private String hostelId;
    private Boolean isDeleted;
    private Boolean isActive;
    private String createdBy;
    private String updatedBy;
    private String createdByUserType;
    private Date createdAt;
    private Date updatedAt;
}
