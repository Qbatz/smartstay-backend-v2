package com.smartstay.smartstay.dao;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "expense_payments")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExpensePayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String expenseId;
    private String hostelId;
    private String vendorId;
    private Double paidAmount;
    private String paymentMethod;
    private String bankId;

    @Temporal(TemporalType.TIMESTAMP)
    private Date paymentDate;

    private String transactionId;
    private String notes;
    private String imageUrl;
}
