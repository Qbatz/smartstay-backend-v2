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
public class VendorV1 {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer vendorId;

    private String firstName;
    private String lastName;
    private String mobileNumber;
    private String emailId;
    private String businessName;
    private String houseNo;
    private String area;
    private String landMark;
    private String city;
    private int pinCode;
    private String state;
    private String country;
    private String profilePic;
    private String hostelId;
    private boolean isActive;
    private String createdBy;
    private Date createdAt;
    private Date updatedAt;
}
