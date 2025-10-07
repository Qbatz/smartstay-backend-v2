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
    //credit or debit
    private String type;
    //assets or rent or advance or expense
    private String source;
    private String hostelId;
    private Date transactionDate;
    private Date createdAt;
    private String createdBy;
}
