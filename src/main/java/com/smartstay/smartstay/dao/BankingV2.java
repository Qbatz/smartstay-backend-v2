package com.smartstay.smartstay.dao;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;
import java.util.List;


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

    @OneToMany(mappedBy = "bank", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<BankingMethods> bankingMethods;
}
