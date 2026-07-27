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

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    private String upiId;
    private Integer upiApp;
    private String displayName;
    private String description;
    private String cardNumber;
    private Integer cardNetwork;
    private String cardHolderName;
    private Double creditLimit;
    private Date billingCycle;
    private String linkedUpiId;
    private String qrImage;
    private String hostelId;
    private String userId;
    private Double balance;
    private Date createdAt;
    private Date updatedAt;
    private String createdBy;
    private String updatedBy;
}
