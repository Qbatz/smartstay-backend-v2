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
    private String address;
    private String locality;
    private String city;
    private String state;
    private String pincode;

    @OneToOne()
    @JoinColumn(name = "kyc_details_id", referencedColumnName = "id")
    private KycDetails kycDetails;
}
