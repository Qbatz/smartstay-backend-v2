package com.smartstay.smartstay.dao;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class BankingV2 {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String bankId;
    private String displayName;
    private String bankName;
    private String accountNumber;
    private String parentId;
    private String ifscCode;
    private String branchName;
    private String accountHolderName;
    private String transactionType;
    private String accountType;
    private String bankAccountType;
    private String description;
    private String userId;
    private String hostelId;
    private Double balance;
    private boolean isActive;
    private boolean isDeleted;
    private boolean isDefaultAccount;
    private String createdBy;
    private String updatedBy;
    private Date createdAt;
    private Date updatedAt;
    private Date lastTransaction;
    private String platform;
}
