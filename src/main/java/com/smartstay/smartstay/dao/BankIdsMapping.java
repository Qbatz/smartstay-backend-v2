package com.smartstay.smartstay.dao;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BankIdsMapping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String oldBankId;
    private String newBankId;
    private String oldBankAccountType;
    private String newBankAccountType;
    //UPI or Bank no need for cash accounts
    private String oldBankPaymentType;
    private String newBankPaymentType;
}
