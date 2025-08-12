package com.smartstay.smartstay.dao;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Assets {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String assetId;

    private String assetName;
    private String productName;
    private String vendorId;
    private String brandName;
    private String serialNumber;
    private Date purchaseDate;
    private Double price;
    private String modeOfPayment;
    private Date createdAt;
    private String createdBy;
    private Date updatedAt;
    private Boolean isActive;
    private String hostelId;
    private String parentId;
}