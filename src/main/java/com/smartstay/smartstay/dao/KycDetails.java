package com.smartstay.smartstay.dao;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class KycDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String currentStatus;
    private String transactionId;
    private String referenceId;

    @OneToOne()
    @JoinColumn(name = "customer_id", referencedColumnName = "customerId")
    private Customers customers;
}
