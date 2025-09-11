package com.smartstay.smartstay.dao;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionV1 {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String transactionId;
    private String type;
    private Double amount;
    private Double balanceAmount;
    private String createdBy;
    private Date createdAt;
    private String status;
    private String invoiceId;
    private String isInvoice;
    private String mode;
    private Date paidAt;
    private String updatedBy;

    @ManyToOne()
    @JoinColumn(name = "customer_id")
    private Customers customers;
}
