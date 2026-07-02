package com.smartstay.smartstay.dao;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class KycAddressDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    //full address
    private String currentAddress;
    private String currentLocality;
    private String currentCity;
    private String currentState;
    private String currentPincode;

    //full address
    private String permanentAddress;
    private String permanentLocality;
    private String permanentCity;
    private String permanentState;
    private String permanentPincode;

    @OneToOne()
    @JoinColumn(name = "kyc_details_id", referencedColumnName = "id")
    private KycDetails kycDetails;
}
