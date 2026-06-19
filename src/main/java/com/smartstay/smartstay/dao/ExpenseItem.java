package com.smartstay.smartstay.dao;

import com.smartstay.smartstay.ennum.ExpensePaymentStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "expense_items")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExpenseItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String expenseId;
    private String hostelId;
    private String vendorId;
    private String item;
    private Integer quantity;
    private Integer unitId;
    private String unit;
    private Double unitPrice;
    private Double totalAmount;

    // Per-item payment details, initialized from the parent expense on creation.
    @Enumerated(EnumType.STRING)
    private ExpensePaymentStatus paymentStatus;
    private Double paidAmount;
}
