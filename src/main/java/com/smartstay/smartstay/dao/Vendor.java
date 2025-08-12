package com.smartstay.smartstay.dao;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Vendor {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String vendorId;

    private String firstName;
    private String lastName;
    private String mobileNumber;
    private String emailId;
    private String businessName;
    private String houseNo;
    private String area;
    private String landMark;
    private String city;
    private String pinCode;
    private String state;
    private String country;
    private String profilePic;
    private String hostelId;
    private String parentId;
}
