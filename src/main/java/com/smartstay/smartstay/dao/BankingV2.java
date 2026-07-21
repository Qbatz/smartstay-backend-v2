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
    String bankId;
    String displayName;
    String bankName;
    String accountNumber;
    String parentId;
    String ifscCode;
    String branchName;
    String accountHolderName;
    String transactionType;
    String accountType;
    String bankAccountType;
    String description;
    String userId;
    String hostelId;
    Double balance;
    boolean isActive;
    boolean isDeleted;
    boolean isDefaultAccount;
    String createdBy;
    String updatedBy;
    Date createdAt;
    Date updatedAt;
    Date lastTransaction;
    private String platform;
    private String cashAccountType;
    private String responsiblePerson;
}
