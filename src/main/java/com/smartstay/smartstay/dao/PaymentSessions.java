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
public class PaymentSessions {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sessionId;
    private String hostelId;
    private Double paymentAmount;
    private Double discountAmount;
    private Double planAmount;
    private String planCode;
    private String paymentSessionId;
    private String paymentStaus;
    private Date createdAt;
    private String createdBy;
}
