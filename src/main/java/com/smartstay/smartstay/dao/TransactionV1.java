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
    private double amount;
    private String createdBy;
    private Date createdAt;
    private String status;

    @ManyToOne()
    @JoinColumn(name = "transactions")
    private Customers customers;
}
