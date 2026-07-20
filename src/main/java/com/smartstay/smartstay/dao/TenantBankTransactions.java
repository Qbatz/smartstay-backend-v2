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
public class TenantBankTransactions {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transactionId;
    private Double transactionAmount;
    private Date transactionDate;
    private String customerId;
    private String hostelId;
    private String relationName;
    private String relationMobile;
    private String relationId;
    private String typeOfRelation;
    //invoice id
    private String sourceId;
    //android/ios/web
    private String platform;
    //tye should be credit/debit  from Bank Transaction Type
    private String transactionType;
    private Double balanceAmount;
    private String createdBy;
    private Date createdAt;
}
