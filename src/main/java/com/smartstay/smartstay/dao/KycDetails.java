package com.smartstay.smartstay.dao;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Data
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class KycDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String currentStatus;
    private String transactionId;
    private String referenceId;
    private Date createdAt;
    private String createdBy;
    private String updatedAt;
    private String updatedBy;

    @OneToOne()
    @JoinColumn(name = "customer_id", referencedColumnName = "customerId")
    private Customers customers;
}
