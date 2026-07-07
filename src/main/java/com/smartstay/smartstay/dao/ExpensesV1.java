package com.smartstay.smartstay.dao;

import com.smartstay.smartstay.ennum.ExpensePaymentStatus;
import com.smartstay.smartstay.handlers.StringListConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

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
    // Final payable amount (after discount); used by all existing financial calculations.
    private Double totalPrice;
    // Original total amount before any discount; for display/reporting only.
    private Double actualTotalPrice;
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
    private String updatedBy;
    private boolean isActive;
    private String description;

    private String title;
    private Boolean isVendorExpense;

    @Enumerated(EnumType.STRING)
    private ExpensePaymentStatus paymentStatus;

    private Double paidAmount;
    private Double balanceAmount;
    private String paymentMethod;
    private String note;
    private Integer creditPeriod;

    // Payment/transaction reference for the expense.
    private String transactionId;
    // Tax amount applicable to the expense.
    private Double tax;
    // Discount amount applied to the expense.
    private Double discount;
    // Effective discount percentage (derived from the discount amount / total, or supplied directly).
    private Double discountPercentage;
    // S3 URLs of the images uploaded with the expense.
    @Convert(converter = StringListConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<String> images;
}