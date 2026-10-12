package com.smartstay.smartstay.dao;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

import java.util.Date;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TempBankTransactionsV1 {
    @Id
    private Integer transactionId;
    private String bankId;
    //user entered
    private String referenceNumber;
    private Double amount;
    private Double accountBalance;
    private String description;
    //credit or debit from BankTransaction Type enum
    private String type;
    //assets or rent or advance or expense from BankSource Enum
    private String source;
    private String sourceId;
    //banking_methods id
    private String paymentMethodId;
    private String investorName;
    private String hostelId;
    //transactionId from transaction v1 table
    private String transactionNumber;
    private Date transactionDate;
    private Boolean isDeleted;
    private Date createdAt;
    private String createdBy;
    private Date updatedAt;
    private String updatedBy;
    private String platform;
}
