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
    //From KYC status enum
    private String currentStatus;
    private String transactionId;
    private String entityId;
    private String templateId;
    private String accessTokenId;
    private String referenceId;
    private Date createdAt;
    private String createdBy;
    private String updatedAt;
    private String updatedBy;
    private Date expireAt;

    @OneToOne()
    @JoinColumn(name = "customer_id", referencedColumnName = "customerId")
    private Customers customers;
}
