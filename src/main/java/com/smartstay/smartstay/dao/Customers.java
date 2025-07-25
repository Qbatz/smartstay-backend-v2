package com.smartstay.smartstay.dao;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Customers {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String customerId;
    private String firstName;
    private String lastName;
    private String mobile;
    private String emailId;
    private String houseNo;
    private String street;
    private String landmark;
    private int pincode;
    private String city;
    private String state;
    private Long country;
    private String profilePic;
    private Date joiningDate;
    private String currentStatus;
    private String kycStatus;
    private String createdBy;
    private Date createdAt;

    @OneToOne(mappedBy = "customers")
    private Advance advance;

    @OneToOne(mappedBy = "customers")
    private KycDetails kycDetails;
}
