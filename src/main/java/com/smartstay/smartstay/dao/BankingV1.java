package com.smartstay.smartstay.dao;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Primary;

import java.util.Date;

@Entity
@Data
@Getter
@Setter
public class BankingV1 {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String bankId;
    String bankName;
    String accountNumber;
    String ifscCode;
    String branchName;
    String branchCode;
    String accountHolderName;
    String transactionType;
    String upiId;
    String creditCardNumber;
    String debitCardNumber;
    String accountType;
    String description;
    boolean isActive;
    boolean isDeleted;
    boolean isDefaultAccount;
    String createdBy;
    String updatedBy;
    Date createdAt;
    Date updatedAt;
}
