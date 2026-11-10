package com.smartstay.smartstay.dao;

import com.smartstay.smartstay.ennum.PaymentMethod;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BankingMethods {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String paymentMethodId;

    @ManyToOne
    @JoinColumn(name = "bank_id")
    private BankingV2 bank;

    //PaymentMethod ENUM
    private String paymentMethod;

    private String upiId;
    private String upiApp;
    private String accountNumber;
    private String displayName;
    private String description;
    private String cardNumber;
    private String cardNetwork;
    private String cardHolderName;
    private Double creditLimit;
    private Integer billingCycle;
    private String linkedUpiId;
    private String qrImage;
    private String hostelId;
    private String userId;
    private Double balance;
    private Date createdAt;
    private Date updatedAt;
    private String createdBy;
    private String updatedBy;
    private Boolean isDeleted;
}
