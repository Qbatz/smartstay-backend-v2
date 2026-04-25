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
    private String paymentType;
    //incase of type is card
    private String cardHolderName;
    //credit or debit
    private String cardType;
    //Visa, rupay, mastercard ....
    private String cardBrand;
    //Icici, Hdfc, SBI
    private String issuer;
    private String cardNo;
    //UPI or bank
    private String channel;
    private String upiId;
    private String userType;
    private String paymentProof;
    //user
    private String paidBy;
    //agent
    private String collectedBy;
    private boolean isActive;
    private Date createdAt;
    //agent && user
    private String createdBy;
}
