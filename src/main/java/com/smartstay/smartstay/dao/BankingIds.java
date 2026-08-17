package com.smartstay.smartstay.dao;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

import java.util.Date;

@Entity
@Data
@Getter
@Setter
public class BankingIds {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    private String bankIdV1;
    private String bankIdV2;
    private String bankAccountType;
    private String paymentMethodIdV1;
    private String paymentMethodIdV2;
    private String paymentMethod;
    private Date migratedAt;
}
