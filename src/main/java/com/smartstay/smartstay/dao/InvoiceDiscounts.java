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
public class InvoiceDiscounts {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long discountId;
    private String invoiceId;
    private String hostelId;
    private String customerId;
    private String discountReason;
    private Double discountAmount;
    private Double discountPercentage;
    //total invoice amount
    private Double invoiceAmount;
    private boolean isActive;
    private Date createdAt;
    private String createdBy;
    private String updatedBy;
    private Date updatedAt;
}
