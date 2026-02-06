package com.smartstay.smartstay.dao;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BankTransactionsV1 {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int transactionId;
    private String bankId;
    private String referenceNumber;
    private Double amount;
    private Double accountBalance;
    private String description;
    //credit or debit
    private String type;
    //assets or rent or advance or expense
    private String source;
    private String sourceId;
    private String hostelId;
    //transactionId from transaction v1 table
    private String transactionNumber;
    private Date transactionDate;
    private Date createdAt;
    private String createdBy;
}
