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
@NoArgsConstructor
@AllArgsConstructor
public class CustomerAdditionalContacts {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long contactId;
    private String name;
    private String relationship;
    private String occupation;
    private String mobile;
    private String fullAddress;
    private String customerId;
    private String hostelId;
    private String countryCode;
    //From userType Enum
    private String addedByUserType;
    //From userType Enum
    private String updatedByUserType;
    private boolean isDeleted;
    private String createdBy;
    private String updatedBy;
    private Date createdAt;
    private Date updatedAt;
}
