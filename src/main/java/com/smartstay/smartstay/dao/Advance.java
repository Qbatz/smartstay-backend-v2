package com.smartstay.smartstay.dao;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Getter
@Setter
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Advance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private double advanceAmount;
    private double paidAmount;
    private Date invoiceDate;
    private Date dueDate;
    private int status;
    private Date createdAt;
    private Date updatedAt;
    private String createdBy;

    @OneToOne()
    @JoinColumn(name = "customer_id", referencedColumnName = "customerId")
    private Customers customers;
}
