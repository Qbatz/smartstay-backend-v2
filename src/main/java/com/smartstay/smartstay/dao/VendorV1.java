package com.smartstay.smartstay.dao;

import com.smartstay.smartstay.ennum.VendorPaymentStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
    private String countryCode;
    // Country/dialing code for the business mobile number (e.g. "+91"). Mandatory.
    private String businessMobileCode;
    private String mobile;
    private String emailId;
    private String businessName;
    private String houseNo;
    private String area;
    private String landMark;
    private String city;
    private int pinCode;
    private String state;
    private Long country;
    private String profilePic;
    private String hostelId;
    private boolean isActive;
    private String createdBy;
    private Date createdAt;
    private Date updatedAt;

    private Integer vendorCategory;
    private String contactPerson;
    private String contactPersonMobile;
    // Country/dialing code for the contact person's mobile (e.g. "+91"). Optional.
    private String contactPersonMobileCode;
    private String description;
    private String vendorCode;
    private String gst;
    private String pan;
    private Boolean allowCredit;
    private Double creditLimit;
    private Integer creditPeriod;

    // Denormalized financial summary, kept in sync on every expense/payment write so the
    // listing API never has to aggregate at read time.
    @Enumerated(EnumType.STRING)
    private VendorPaymentStatus paymentStatus;
    private Double totalExpense;
    private Double totalPaid;
    private Double balance;
}
