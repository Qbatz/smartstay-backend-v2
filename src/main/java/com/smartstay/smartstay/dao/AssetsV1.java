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
public class AssetsV1 {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer assetId;

    private String assetName;
    private String productName;
    private int vendorId;
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