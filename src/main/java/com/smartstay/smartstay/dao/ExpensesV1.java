package com.smartstay.smartstay.dao;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExpensesV1 {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String expenseId;

    private Date createdAt;
    private Date updatedAt;
    private String createdBy;
    private Boolean isActive;
    private String categoryId;
    private Date purchaseDate;
    private String hostelId;
    private int unitCount;
    private Double perUnitAmount;
    private Double purchaseAmount;
    private String modeOfTransaction;
    private String description;
    private String parentId;
}