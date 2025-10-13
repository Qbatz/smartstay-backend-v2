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
    private Long categoryId;
    private Long subCategoryId;
    private String parentId;
    private String hostelId;
    private String bankId;

//    this is for single product amount
    private Double unitPrice;
    private Integer unitCount;
    private Double totalPrice;
    private Double gst;
    private Double cgst;
    private Double sgst;
    private Double gstAmount;
    private Double cgstAmount;
    private Double sgstAmount;
    private Double discounts;
    private Double discountAmount;
    private String expenseNumber;
    //final amount after discount and gst
    private Double transactionAmount;
    private String vendorId;
    //from expense source
    private String source;

    private Date transactionDate;
    private Date createdAt;
    private Date updatedAt;
    private String createdBy;
    private boolean isActive;
    private String description;
}