package com.smartstay.smartstay.dao;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class HostelV1 {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String hostelId;
    private String hostelName;
    private String mobile;
    private String emailId;
    private String mainImage;
    private String houseNo;
    private String street;
    private String landmark;
    private String pincode;
    private String city;
    private String state;
    private String country;
}
