package com.smartstay.smartstay.dao;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 *
 * backup for bank transactions v1
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TempTransactions {
    @Id
    private int transactionId;
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
