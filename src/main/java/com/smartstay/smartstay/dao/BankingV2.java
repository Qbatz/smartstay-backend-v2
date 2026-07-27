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
    private String cashAccountType;
    private String responsiblePerson;

    @OneToMany(mappedBy = "bank", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<BankingMethods> bankingMethods;
}
