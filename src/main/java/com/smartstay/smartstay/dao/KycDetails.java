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
    private Date completedAt;
    private String aadhaarNumber;
    //pdf of Aadhaar or pan
    private String kycDocument;
    //Aadhaar or PAN, or any govt id proof
    private String kycDocumentType;
    private String documentType;
    private String gender;
    private String idPic;
    private String nameInDocument;
    private String dateOfBirth;
    private String permanentAddress;
    private String createdBy;
    private Date updatedAt;
    private Date expireAt;

    @OneToOne(mappedBy = "kycDetails",  cascade = CascadeType.ALL, orphanRemoval = true)
    private KycAddressDetails addressDetails;


    @OneToOne()
    @JoinColumn(name = "customer_id", referencedColumnName = "customerId")
    private Customers customers;

}
