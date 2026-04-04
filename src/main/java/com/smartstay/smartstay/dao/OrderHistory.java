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
public class OrderHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long historyId;
    private String hostelId;
    private String paymentUrl;
    private String paymentLinkId;
    private Double discountAmount;
    private Double planAmount;
    private String planCode;
    private String planName;
    private Double totalAmount;
    //order status enum
    private String orderStatus;
    private String userType;
    private boolean isActive;
    private Date createdAt;
    private String createdBy;
}
