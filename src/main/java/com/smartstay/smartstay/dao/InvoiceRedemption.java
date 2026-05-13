package com.smartstay.smartstay.dao;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

import java.util.Date;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceRedemption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String sourceInvoiceId;
    private String targetInvoiceId;
    private String hostelId;
    private Double redemptionAmount;
    private String referenceNumber;
    private String transactionId;
    private Boolean isActive;
    private String reason;
    private Date redeemedAt;
    private String createdBy;
    private String updatedBy;
    private String userType;
    private Date createdAt;
    private Date updatedAt;
}
