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
    private String reason;
    private Date redeemedAt;
    private Date createdAt;
    private String createdBy;
}
